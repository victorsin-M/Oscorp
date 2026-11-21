package com.mycompany.soptec.it.excepciones;

/**
 * Error al leer, copiar o almacenar archivos de la aplicacion (imagenes, croquis, evidencias),
 * por ejemplo falta de permisos, disco lleno o carpeta inaccesible.
 */
public class ArchivoException extends SoptecException {

    public ArchivoException(String modulo, String operacion, String mensajeUsuario) {
        super("ARCHIVO", modulo, operacion, mensajeUsuario);
    }

    public ArchivoException(String modulo, String operacion, String mensajeUsuario, Throwable causa) {
        super("ARCHIVO", modulo, operacion, mensajeUsuario, causa);
    }
}
