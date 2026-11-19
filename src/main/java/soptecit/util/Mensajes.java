package soptecit.util;

import com.mycompany.soptec.it.util.AlertaUtil;

/**
 * Puente de compatibilidad para mostrar avisos con el estilo institucional de SOPTEC-IT.
 * Delega en AlertaUtil (que aplica estilos.css y vincula la ventana activa).
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(String titulo, String mensaje) {
        AlertaUtil.info(titulo, mensaje);
    }

    public static void advertencia(String titulo, String mensaje) {
        AlertaUtil.advertencia(titulo, mensaje);
    }

    public static void error(String titulo, String mensaje) {
        AlertaUtil.error(titulo, mensaje);
    }

    public static boolean confirmar(String titulo, String mensaje) {
        return AlertaUtil.confirmar(titulo, mensaje);
    }
}
