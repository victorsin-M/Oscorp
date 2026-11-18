package com.mycompany.soptec.it.excepciones;

/**
 * Error por restriccion de integridad referencial (claves foraneas o registros dependientes).
 */
public class IntegridadReferencialException extends SoptecException {

    public IntegridadReferencialException(String modulo, String operacion, String mensajeUsuario) {
        super("INTEGRIDAD_REFERENCIAL", modulo, operacion, mensajeUsuario);
    }

    public IntegridadReferencialException(String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super("INTEGRIDAD_REFERENCIAL", modulo, operacion, mensajeUsuario, causa);
    }
}
