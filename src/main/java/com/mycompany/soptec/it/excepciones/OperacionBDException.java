package com.mycompany.soptec.it.excepciones;

/**
 * Error tecnico durante operaciones CRUD, consultas o transacciones en la base de datos.
 */
public class OperacionBDException extends SoptecException {

    public OperacionBDException(String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super("OPERACION_BD", modulo, operacion, mensajeUsuario, causa);
    }

    public OperacionBDException(String modulo, String operacion, String mensajeUsuario) {
        super("OPERACION_BD", modulo, operacion, mensajeUsuario);
    }
}
