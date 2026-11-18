package com.mycompany.soptec.it.excepciones;

/**
 * Excepcion base del sistema SOPTEC-IT.
 * Separa el mensaje amigable para el usuario del detalle tecnico registrado en el log.
 */
public class SoptecException extends Exception {

    private final String categoria;
    private final String modulo;
    private final String operacion;

    public SoptecException(String categoria, String modulo, String operacion, String mensajeUsuario) {
        super(mensajeUsuario);
        this.categoria = categoria;
        this.modulo = modulo;
        this.operacion = operacion;
    }

    public SoptecException(String categoria, String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super(mensajeUsuario, causa);
        this.categoria = categoria;
        this.modulo = modulo;
        this.operacion = operacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getModulo() {
        return modulo;
    }

    public String getOperacion() {
        return operacion;
    }
}
