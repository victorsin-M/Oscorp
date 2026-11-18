package com.mycompany.soptec.it.util;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Administra la copia, validacion y carga segura de imagenes locales (clientes, empleados, proveedores, croquis).
 */
public final class GestorImagenes {

    private static final Set<String> EXTENSIONES_VALIDAS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final DateTimeFormatter FORMATO_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private GestorImagenes() {
    }

    public static String elegirYGuardarImagen(Window ventana, String titulo, String subcarpeta, String modulo)
            throws ValidacionException {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.webp)",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        File seleccionado = selector.showOpenDialog(ventana);
        if (seleccionado == null) {
            return null;
        }
        return guardarImagen(seleccionado, subcarpeta, modulo);
    }

    public static String guardarImagen(File archivoOrigen, String subcarpeta, String modulo) throws ValidacionException {
        if (archivoOrigen == null) {
            return null;
        }
        if (!archivoOrigen.exists() || !archivoOrigen.isFile()) {
            throw new ValidacionException(modulo, "GESTION_IMAGEN", "El archivo de imagen seleccionado no existe.");
        }
        String nombre = archivoOrigen.getName();
        String extension = obtenerExtension(nombre);
        if (!EXTENSIONES_VALIDAS.contains(extension)) {
            throw new ValidacionException(modulo, "GESTION_IMAGEN",
                    "Formato de imagen no permitido. Usa PNG, JPG, JPEG, GIF o WEBP.");
        }
        try {
            Path carpetaDestino = Paths.get("uploads", subcarpeta);
            if (!Files.exists(carpetaDestino)) {
                Files.createDirectories(carpetaDestino);
            }
            String nombreSeguro = LocalDateTime.now().format(FORMATO_TS) + "_"
                    + nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destino = carpetaDestino.resolve(nombreSeguro);
            Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            return Paths.get("uploads", subcarpeta, nombreSeguro).toString().replace('\\', '/');
        } catch (IOException ex) {
            AppLogger.registrarError(modulo, "GestorImagenes", "COPIAR_IMAGEN", "ARCHIVO",
                    "Error al copiar imagen al directorio uploads", ex);
            throw new ValidacionException(modulo, "GESTION_IMAGEN",
                    "No se pudo almacenar la imagen seleccionada. Verifica permisos de escritura.", ex);
        }
    }

    public static Image cargarImagen(String rutaRelativaOAbsoluta) {
        if (rutaRelativaOAbsoluta == null || rutaRelativaOAbsoluta.isBlank()) {
            return null;
        }
        try {
            Path ruta = Paths.get(rutaRelativaOAbsoluta);
            if (!Files.exists(ruta)) {
                return null;
            }
            return new Image(ruta.toUri().toString(), true);
        } catch (Exception ex) {
            AppLogger.registrarError("Sistema", "GestorImagenes", "CARGAR_IMAGEN", "ARCHIVO",
                    "No se pudo cargar la imagen: " + rutaRelativaOAbsoluta, ex);
            return null;
        }
    }

    private static String obtenerExtension(String nombre) {
        int idx = nombre.lastIndexOf('.');
        if (idx < 0 || idx == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(idx + 1).toLowerCase();
    }
}
