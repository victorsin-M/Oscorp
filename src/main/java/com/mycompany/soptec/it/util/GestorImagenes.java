package com.mycompany.soptec.it.util;

import com.mycompany.soptec.it.excepciones.ArchivoException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Administra la copia, validacion y carga segura de imagenes locales
 * (clientes, empleados, proveedores, croquis y evidencias de instalacion).
 *
 * <p>Valida la firma binaria real del archivo, limite de 10 MB y realiza copia atomica
 * para nunca dejar archivos corruptos o a medias. Diferencia errores de validacion
 * ({@link ValidacionException}) de errores de almacenamiento/permisos ({@link ArchivoException}).</p>
 */
public final class GestorImagenes {

    public static final String NOMBRE_CARPETA_APP = "SOPTEC-IT";
    public static final Set<String> EXTENSIONES_VALIDAS = Set.of("png", "jpg", "jpeg", "gif", "bmp");
    public static final Set<String> SUBCARPETAS_VALIDAS = Set.of("clientes", "empleados", "proveedores", "croquis", "evidencias");
    /** Tamano maximo permitido por imagen: 10 MB. */
    public static final long TAMANO_MAXIMO_BYTES = 10L * 1024 * 1024;

    private static final DateTimeFormatter FORMATO_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final String OPERACION = "GESTION_IMAGEN";

    private static volatile Path directorioBase = directorioPorDefecto();

    private GestorImagenes() {
    }

    public static Path directorioPorDefecto() {
        String personalizada = System.getProperty("soptec.imagenes.dir");
        if (personalizada != null && !personalizada.isBlank()) {
            return Paths.get(personalizada).toAbsolutePath();
        }
        return Paths.get(System.getProperty("user.home"), "Documents", NOMBRE_CARPETA_APP, "imagenes")
                .toAbsolutePath();
    }

    public static Path getDirectorioBase() {
        return directorioBase;
    }

    public static void configurarDirectorioBase(Path nuevoDirectorio) {
        directorioBase = nuevoDirectorio == null ? directorioPorDefecto() : nuevoDirectorio.toAbsolutePath();
    }

    public static void restaurarDirectorioPorDefecto() {
        directorioBase = directorioPorDefecto();
    }

    public static Path asegurarCarpetaAplicacion(String modulo) throws ArchivoException {
        return asegurarDirectorio(directorioBase, modulo);
    }

    public static String elegirYGuardarImagen(Window ventana, String titulo, String subcarpeta, String modulo)
            throws SoptecException {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File seleccionado = selector.showOpenDialog(ventana);
        if (seleccionado == null) {
            return null;
        }
        return guardarImagen(seleccionado, subcarpeta, modulo);
    }

    public static String guardarImagen(File archivoOrigen, String subcarpeta, String modulo) throws SoptecException {
        if (archivoOrigen == null) {
            return null;
        }
        try {
            validarSubcarpeta(subcarpeta, modulo);
            Path origen = archivoOrigen.toPath();
            validarArchivoImagen(origen, modulo);

            Path carpetaDestino = asegurarDirectorio(directorioBase.resolve(subcarpeta), modulo);
            String nombreSeguro = LocalDateTime.now().format(FORMATO_TS) + "_"
                    + archivoOrigen.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destino = carpetaDestino.resolve(nombreSeguro);
            copiarAtomicamente(origen, destino, modulo);

            String rutaRelativa = subcarpeta + "/" + nombreSeguro;
            AppLogger.registrarInfo(modulo, "GestorImagenes", "GUARDAR_IMAGEN",
                    "Imagen almacenada en " + rutaRelativa);
            return rutaRelativa;
        } catch (SoptecException ex) {
            AppLogger.registrarExcepcion(modulo, "GestorImagenes", "GUARDAR_IMAGEN", ex);
            throw ex;
        }
    }

    public static void validarArchivoImagen(Path archivo, String modulo) throws SoptecException {
        if (archivo == null || !Files.exists(archivo) || !Files.isRegularFile(archivo)) {
            throw new ValidacionException(modulo, OPERACION,
                    "El archivo de imagen seleccionado no existe o fue movido. Elige otra imagen.");
        }
        String extension = obtenerExtension(archivo.getFileName().toString());
        if (!EXTENSIONES_VALIDAS.contains(extension)) {
            throw new ValidacionException(modulo, OPERACION,
                    "Formato de imagen no permitido. Usa PNG, JPG, JPEG, GIF o BMP.");
        }
        if (!Files.isReadable(archivo)) {
            throw new ArchivoException(modulo, OPERACION,
                    "No se tienen permisos para leer la imagen seleccionada.");
        }
        try {
            long tamano = Files.size(archivo);
            if (tamano == 0) {
                throw new ValidacionException(modulo, OPERACION, "La imagen seleccionada está vacía o dañada.");
            }
            if (tamano > TAMANO_MAXIMO_BYTES) {
                throw new ValidacionException(modulo, OPERACION,
                        "La imagen excede el tamaño máximo permitido de 10 MB.");
            }
            if (!tieneFirmaDeImagen(archivo)) {
                throw new ValidacionException(modulo, OPERACION,
                        "El archivo seleccionado no es una imagen válida aunque su extensión lo indique.");
            }
        } catch (IOException ex) {
            throw new ArchivoException(modulo, OPERACION,
                    "No se pudo leer la imagen seleccionada. Verifica que no esté abierta en otro programa.", ex);
        }
    }

    public static Path resolverRuta(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            return null;
        }
        try {
            Path ruta = Paths.get(rutaGuardada.trim());
            if (ruta.isAbsolute()) {
                return ruta;
            }
            Path enCarpetaApp = directorioBase.resolve(ruta).normalize();
            if (Files.exists(enCarpetaApp) || !Files.exists(ruta)) {
                return enCarpetaApp;
            }
            return ruta.toAbsolutePath();
        } catch (InvalidPathException ex) {
            return null;
        }
    }

    public static boolean existeImagen(String rutaGuardada) {
        Path ruta = resolverRuta(rutaGuardada);
        return ruta != null && Files.isRegularFile(ruta);
    }

    public static byte[] leerImagen(String rutaGuardada, String modulo) throws SoptecException {
        Path ruta = resolverRuta(rutaGuardada);
        if (ruta == null || !Files.isRegularFile(ruta)) {
            ValidacionException ex = new ValidacionException(modulo, "LEER_IMAGEN",
                    "La imagen registrada ya no se encuentra en la carpeta de la aplicación.");
            AppLogger.registrarExcepcion(modulo, "GestorImagenes", "LEER_IMAGEN", ex);
            throw ex;
        }
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException ex) {
            ArchivoException ae = new ArchivoException(modulo, "LEER_IMAGEN",
                    "No se pudo leer la imagen registrada. Verifica los permisos de la carpeta.", ex);
            AppLogger.registrarExcepcion(modulo, "GestorImagenes", "LEER_IMAGEN", ae);
            throw ae;
        }
    }

    public static Image cargarImagen(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            return null;
        }
        Path ruta = resolverRuta(rutaGuardada);
        if (ruta == null || !Files.isRegularFile(ruta)) {
            AppLogger.registrarError("Sistema", "GestorImagenes", "CARGAR_IMAGEN", "ARCHIVO",
                    "Imagen registrada no encontrada: " + rutaGuardada, null);
            return null;
        }
        try {
            Image imagen = new Image(ruta.toUri().toString(), false);
            if (imagen.isError()) {
                AppLogger.registrarError("Sistema", "GestorImagenes", "CARGAR_IMAGEN", "ARCHIVO",
                        "La imagen esta danada o no es compatible: " + rutaGuardada, imagen.getException());
                return null;
            }
            return imagen;
        } catch (Exception ex) {
            AppLogger.registrarError("Sistema", "GestorImagenes", "CARGAR_IMAGEN", "ARCHIVO",
                    "No se pudo cargar la imagen: " + rutaGuardada, ex);
            return null;
        }
    }

    public static boolean eliminarImagen(String rutaGuardada) {
        Path ruta = resolverRuta(rutaGuardada);
        if (ruta == null || !ruta.normalize().startsWith(directorioBase)) {
            return false;
        }
        try {
            return Files.deleteIfExists(ruta);
        } catch (IOException ex) {
            AppLogger.registrarError("Sistema", "GestorImagenes", "ELIMINAR_IMAGEN", "ARCHIVO",
                    "No se pudo eliminar la imagen: " + rutaGuardada, ex);
            return false;
        }
    }

    private static void validarSubcarpeta(String subcarpeta, String modulo) throws ValidacionException {
        if (subcarpeta == null || !SUBCARPETAS_VALIDAS.contains(subcarpeta)) {
            throw new ValidacionException(modulo, OPERACION,
                    "La categoría de imagen indicada no es válida.");
        }
    }

    private static Path asegurarDirectorio(Path carpeta, String modulo) throws ArchivoException {
        try {
            if (!Files.isDirectory(carpeta)) {
                Files.createDirectories(carpeta);
                AppLogger.registrarInfo(modulo, "GestorImagenes", "CREAR_CARPETA",
                        "Carpeta de imagenes creada: " + carpeta);
            }
            if (!Files.isWritable(carpeta)) {
                throw new ArchivoException(modulo, OPERACION,
                        "La carpeta de imágenes de SOPTEC-IT no tiene permisos de escritura.");
            }
            return carpeta;
        } catch (AccessDeniedException ex) {
            throw new ArchivoException(modulo, OPERACION,
                    "Acceso denegado al crear la carpeta de imágenes en Documentos.", ex);
        } catch (IOException ex) {
            throw new ArchivoException(modulo, OPERACION,
                    "No se pudo crear la carpeta de imágenes de SOPTEC-IT en Documentos.", ex);
        }
    }

    private static void copiarAtomicamente(Path origen, Path destino, String modulo) throws ArchivoException {
        Path temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
        try {
            Files.copy(origen, temporal, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignored) {
                AppLogger.registrarError(modulo, "GestorImagenes", "LIMPIAR_TEMPORAL", "ARCHIVO",
                        "No se pudo limpiar archivo temporal de imagen", ignored);
            }
            throw new ArchivoException(modulo, OPERACION,
                    "No se pudo almacenar la imagen seleccionada. Verifica el espacio en disco y los permisos.", ex);
        }
    }

    private static boolean tieneFirmaDeImagen(Path archivo) throws IOException {
        byte[] cabecera = new byte[8];
        int leidos;
        try (InputStream in = Files.newInputStream(archivo)) {
            leidos = in.readNBytes(cabecera, 0, cabecera.length);
        }
        if (leidos < 2) {
            return false;
        }
        byte[] b = Arrays.copyOf(cabecera, leidos);
        boolean png = leidos >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
        boolean jpg = leidos >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean gif = leidos >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
        boolean bmp = b[0] == 'B' && b[1] == 'M';
        return png || jpg || gif || bmp;
    }

    private static String obtenerExtension(String nombre) {
        int idx = nombre.lastIndexOf('.');
        if (idx < 0 || idx == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(idx + 1).toLowerCase();
    }
}
