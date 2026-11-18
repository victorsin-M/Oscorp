package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.ConexionBDException;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Utilidad comun para los DAOs: mapeo uniforme de SQLException, logging y parametros nulos.
 */
final class DaoSupport {

    private DaoSupport() {
    }

    static SoptecException traducirYRegistrar(String modulo, String proceso, String operacion,
            String mensajeUsuarioDefecto, SQLException ex) {
        int code = ex.getErrorCode();
        String sqlState = ex.getSQLState() == null ? "" : ex.getSQLState();

        if (code == 1062) {
            ValidacionException ve = new ValidacionException(modulo, operacion,
                    "Ya existe un registro duplicado con los datos proporcionados.", ex);
            AppLogger.registrarError(modulo, proceso, operacion, "VALIDACION_DUPLICADO",
                    "Restriccion UNIQUE violada", ex);
            return ve;
        }
        if (code == 1451) {
            IntegridadReferencialException ire = new IntegridadReferencialException(modulo, operacion,
                    "No se puede eliminar o modificar el registro porque tiene registros relacionados en otra tabla.", ex);
            AppLogger.registrarError(modulo, proceso, operacion, "INTEGRIDAD_REFERENCIAL",
                    "Restriccion FK al eliminar padre (error 1451)", ex);
            return ire;
        }
        if (code == 1452) {
            IntegridadReferencialException ire = new IntegridadReferencialException(modulo, operacion,
                    "El registro relacionado indicado no existe en la base de datos.", ex);
            AppLogger.registrarError(modulo, proceso, operacion, "INTEGRIDAD_REFERENCIAL",
                    "Restriccion FK al referenciar padre inexistente (error 1452)", ex);
            return ire;
        }
        if (sqlState.startsWith("08")) {
            ConexionBDException cbe = new ConexionBDException(modulo, operacion,
                    "Se perdió la conexión con la base de datos. Verifica el servidor MySQL e intenta nuevamente.", ex);
            AppLogger.registrarError(modulo, proceso, operacion, "CONEXION_BD",
                    "Fallo de enlace o comunicacion con MySQL", ex);
            return cbe;
        }

        OperacionBDException obe = new OperacionBDException(modulo, operacion, mensajeUsuarioDefecto, ex);
        AppLogger.registrarError(modulo, proceso, operacion, "OPERACION_BD",
                "Error SQL en operacion de base de datos", ex);
        return obe;
    }

    static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null || value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    static void setNullableDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, java.sql.Date.valueOf(date));
        }
    }
}
