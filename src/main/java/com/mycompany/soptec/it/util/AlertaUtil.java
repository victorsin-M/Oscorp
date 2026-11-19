package com.mycompany.soptec.it.util;

import com.mycompany.soptec.it.excepciones.SoptecException;
import java.net.URL;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Utilidad para mostrar mensajes claros y comprensibles al usuario sin exponer trazas SQL,
 * aplicando la hoja de estilos institucional de SOPTEC-IT.
 */
public final class AlertaUtil {

    private static final String CSS = "/soptecit/css/estilos.css";

    private AlertaUtil() {
    }

    public static void info(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    public static void advertencia(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    public static void errorControlado(String modulo, String operacion, Throwable ex) {
        if (ex instanceof SoptecException) {
            SoptecException se = (SoptecException) ex;
            String titulo;
            switch (se.getCategoria()) {
                case "VALIDACION":
                    titulo = "Datos inválidos o incompletos";
                    break;
                case "INTEGRIDAD_REFERENCIAL":
                    titulo = "Operación bloqueada por registros relacionados";
                    break;
                case "CONEXION_BD":
                    titulo = "Sin conexión con la base de datos";
                    break;
                default:
                    titulo = "Error al procesar la operación";
                    break;
            }
            mostrar(Alert.AlertType.WARNING, titulo, se.getMessage()
                    + "\n\nEl detalle técnico quedó registrado en el archivo de log.");
        } else {
            AppLogger.registrarExcepcion(modulo, "ControladorUI", operacion, ex);
            mostrar(Alert.AlertType.ERROR, "Error inesperado",
                    "Ocurrió un problema inesperado al realizar la operación, pero el sistema continúa funcionando.\n"
                    + "Consulta el módulo 'Logs y Pruebas' para revisar el detalle técnico.");
        }
    }

    public static boolean confirmar(String titulo, String mensaje) {
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, titulo, mensaje);
        Optional<ButtonType> res = alerta.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        crear(tipo, titulo, mensaje).showAndWait();
    }

    private static Alert crear(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("SOPTEC-IT");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        URL cssUrl = AlertaUtil.class.getResource(CSS);
        if (cssUrl != null) {
            alerta.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
        Window activa = ventanaActiva();
        if (activa != null) {
            alerta.initOwner(activa);
        }
        return alerta;
    }

    private static Window ventanaActiva() {
        Window primera = null;
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w.isFocused()) {
                return w;
            }
            if (primera == null && w.isShowing()) {
                primera = w;
            }
        }
        return primera;
    }
}
