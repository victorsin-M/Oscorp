package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.ConexionBDException;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Administra conexiones JDBC con MySQL/MariaDB y soporte transaccional seguro.
 */
public class Conexion {

    private static final String URL_DEFAULT = "jdbc:mysql://localhost:3306/soptec_it?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USUARIO_DEFAULT = "root";
    private static final String PASSWORD_DEFAULT = "";

    private static volatile String urlActual = URL_DEFAULT;
    private static volatile String usuarioActual = USUARIO_DEFAULT;
    private static volatile String passwordActual = PASSWORD_DEFAULT;
    private static volatile boolean simularFalloConexion = false;

    public static void configurar(String url, String usuario, String password) {
        urlActual = (url == null || url.isBlank()) ? URL_DEFAULT : url;
        usuarioActual = (usuario == null) ? USUARIO_DEFAULT : usuario;
        passwordActual = (password == null) ? PASSWORD_DEFAULT : password;
    }

    public static void restaurarConfiguracionPorDefecto() {
        urlActual = URL_DEFAULT;
        usuarioActual = USUARIO_DEFAULT;
        passwordActual = PASSWORD_DEFAULT;
        simularFalloConexion = false;
    }

    public static String getUrlActual() {
        return urlActual;
    }

    public static boolean isSimularFalloConexion() {
        return simularFalloConexion;
    }

    public static void setSimularFalloConexion(boolean simular) {
        simularFalloConexion = simular;
    }

    /**
     * Metodo compatible con clases previas (PruebaConexion).
     */
    public static Connection conectar() {
        try {
            return conectarStrict("Conexion", "CONECTAR");
        } catch (ConexionBDException e) {
            return null;
        }
    }

    /**
     * Abre una conexion JDBC y lanza ConexionBDException con registro en log si falla.
     */
    public static Connection conectarStrict(String modulo, String operacion) throws ConexionBDException {
        if (simularFalloConexion) {
            ConexionBDException ex = new ConexionBDException(modulo, operacion,
                    "No fue posible conectar con la base de datos en este momento. Verifica que MySQL esté encendido e intenta nuevamente.");
            AppLogger.registrarError(modulo, "Conexion", operacion, "CONEXION_BD",
                    "Fallo de conexion simulado hacia " + urlActual + " (usuario=" + usuarioActual + ", password=***)",
                    ex);
            throw ex;
        }
        try {
            return DriverManager.getConnection(urlActual, usuarioActual, passwordActual);
        } catch (SQLException e) {
            AppLogger.registrarError(modulo, "Conexion", operacion, "CONEXION_BD",
                    "Error al conectar con MySQL en " + urlActual + " (usuario=" + usuarioActual + ", password=***)",
                    e);
            throw new ConexionBDException(modulo, operacion,
                    "No se pudo establecer conexión con la base de datos. Verifica que el servicio MySQL esté activo.", e);
        }
    }

    public static boolean verificarConexion() {
        try (Connection con = conectarStrict("Sistema", "VERIFICAR_CONEXION")) {
            return con != null && !con.isClosed();
        } catch (Exception ex) {
            return false;
        }
    }

    public static void revertirSilencioso(Connection con, String modulo, String operacion) {
        if (con == null) {
            return;
        }
        try {
            if (!con.getAutoCommit()) {
                con.rollback();
            }
        } catch (SQLException ex) {
            AppLogger.registrarError(modulo, "Conexion", operacion + "_ROLLBACK", "OPERACION_BD",
                    "Error al ejecutar rollback de transaccion", ex);
        }
    }

    public static void cerrarSilencioso(Connection con) {
        if (con == null) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException ex) {
            AppLogger.registrarError("Sistema", "Conexion", "CERRAR_CONEXION", "OPERACION_BD",
                    "Error al cerrar conexion JDBC", ex);
        }
    }
}
