package com.mycompany.soptec.it.excepciones;

/**
 * Error de validacion de campos, formatos o reglas de negocio (por ejemplo, duplicados).
 */
public class ValidacionException extends SoptecException {

    public ValidacionException(String modulo, String operacion, String mensajeUsuario) {
        super("VALIDACION", modulo, operacion, mensajeUsuario);
    }

    public ValidacionException(String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super("VALIDACION", modulo, operacion, mensajeUsuario, causa);
    }
}
