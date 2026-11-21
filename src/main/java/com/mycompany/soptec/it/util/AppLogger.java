package com.mycompany.soptec.it.util;

import com.mycompany.soptec.it.excepciones.SoptecException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Mecanismo centralizado de registro de errores y eventos de SOPTEC-IT.
 * Escribe en logs/soptec_errores.log protegiendo datos confidenciales.
 */
public final class AppLogger {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path RUTA_LOG_DEFAULT = Paths.get("logs", "soptec_errores.log");
    private static volatile Path rutaLog = RUTA_LOG_DEFAULT;
    private static final Pattern PATRON_SENSIBLE = Pattern.compile(
            "(?i)(password|contrasena|contraseña|pwd|clave|secret|token)\\s*[:=]\\s*[^\\s,;)]*");

    private AppLogger() {
    }

    public static synchronized void configurarRutaLog(Path nuevaRuta) {
        rutaLog = (nuevaRuta == null) ? RUTA_LOG_DEFAULT : nuevaRuta;
    }

    public static synchronized void restaurarRutaPorDefecto() {
        rutaLog = RUTA_LOG_DEFAULT;
    }

    public static synchronized void registrarExcepcion(String modulo, String proceso, String operacion, Throwable ex) {
        String tipo = ex instanceof SoptecException
                ? ((SoptecException) ex).getCategoria()
                : ex.getClass().getSimpleName();
        String detalle = construirDetalleTecnico(ex);
        escribir("ERROR", modulo, proceso, operacion, tipo, detalle);
    }

    public static synchronized void registrarError(String modulo, String proceso, String operacion,
            String tipoExcepcion, String descripcionTecnica, Throwable ex) {
        String detalle = descripcionTecnica == null ? "" : descripcionTecnica;
        if (ex != null) {
            detalle = detalle.isEmpty() ? construirDetalleTecnico(ex) : detalle + " | Causa: " + construirDetalleTecnico(ex);
        }
        escribir("ERROR", modulo, proceso, operacion, tipoExcepcion, detalle);
    }

    public static synchronized void registrarInfo(String modulo, String proceso, String operacion, String mensaje) {
        escribir("INFO", modulo, proceso, operacion, "OPERACION_OK", mensaje);
    }

    public static synchronized List<String> leerLineas() {
        try {
            if (!Files.exists(rutaLog)) {
                return Collections.emptyList();
            }
            return Files.readAllLines(rutaLog, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return Collections.singletonList("No se pudo leer el archivo de log: " + ex.getMessage());
        }
    }

    public static synchronized String leerContenidoCompleto() {
        List<String> lineas = leerLineas();
        return String.join(System.lineSeparator(), lineas);
    }

    public static synchronized void limpiarLog() throws IOException {
        asegurarDirectorio();
        Files.write(rutaLog, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Path getRutaLog() {
        return rutaLog.toAbsolutePath();
    }

    private static void escribir(String nivel, String modulo, String proceso, String operacion,
            String tipoExcepcion, String detalleTecnico) {
        try {
            asegurarDirectorio();
            String fechaHora = LocalDateTime.now().format(FORMATO_FECHA);
            String detalleSeguro = sanitizar(detalleTecnico);
            String linea = String.format(
                    "[%s] [%s] | Modulo=%s | Proceso=%s | Operacion=%s | TipoExcepcion=%s | Detalle=%s%n",
                    fechaHora,
                    nivel,
                    limpiarCampo(modulo),
                    limpiarCampo(proceso),
                    limpiarCampo(operacion),
                    limpiarCampo(tipoExcepcion),
                    detalleSeguro);
            Files.write(rutaLog, linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ioEx) {
            System.err.println("Error critico al escribir log: " + ioEx.getMessage());
        }
    }

    private static void asegurarDirectorio() throws IOException {
        Path parent = rutaLog.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private static String construirDetalleTecnico(Throwable ex) {
        if (ex == null) {
            return "Sin detalle adicional";
        }
        StringBuilder sb = new StringBuilder();
        Throwable actual = ex;
        int profundidad = 0;
        while (actual != null && profundidad < 4) {
            if (profundidad > 0) {
                sb.append(" -> ");
            }
            sb.append(actual.getClass().getSimpleName());
            if (actual instanceof SQLException) {
                SQLException sqlEx = (SQLException) actual;
                sb.append("[SQLState=").append(sqlEx.getSQLState())
                        .append(", Code=").append(sqlEx.getErrorCode()).append("]");
            }
            if (actual.getMessage() != null && !actual.getMessage().isBlank()) {
                sb.append(": ").append(actual.getMessage().replaceAll("[\\r\\n]+", " "));
            }
            actual = actual.getCause();
            profundidad++;
        }
        return sb.toString();
    }

    public static String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        String limpio = PATRON_SENSIBLE.matcher(texto).replaceAll("$1=***");
        limpio = limpio.replaceAll("(?i)(jdbc:mysql://[^?\\s]+)(\\?[^\\s]*)?", "$1");
        return limpio.replaceAll("[\\r\\n]+", " ").trim();
    }

    private static String limpiarCampo(String valor) {
        return valor == null || valor.isBlank() ? "N/A" : valor.replaceAll("[\\r\\n|]+", " ").trim();
    }
}
