package com.mycompany.soptec.it.excepciones;

/**
 * Error al conectar con el servidor MySQL o perdida de conexion.
 */
public class ConexionBDException extends SoptecException {

    public ConexionBDException(String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super("CONEXION_BD", modulo, operacion, mensajeUsuario, causa);
    }

    public ConexionBDException(String modulo, String operacion, String mensajeUsuario) {
        super("CONEXION_BD", modulo, operacion, mensajeUsuario);
    }
}
