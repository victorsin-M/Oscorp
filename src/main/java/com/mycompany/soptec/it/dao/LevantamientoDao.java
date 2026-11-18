package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Levantamiento;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla levantamiento (relacionado con proyecto y opcionalmente con cita).
 */
public class LevantamientoDao {

    private static final String MODULO = "Levantamientos";

    private static final String SELECT_BASE =
            "SELECT l.id_levantamiento, l.fecha_visita, l.croquis, l.observaciones_tecnicas, "
            + "l.activo, l.id_cita, l.id_proyecto, p.nombre_proyecto, c.nombre_cliente, "
            + "CONCAT_WS(' ', ci.fecha_cita, ci.hora_cita) AS fecha_hora_cita "
            + "FROM levantamiento l "
            + "INNER JOIN proyecto p ON p.id_proyecto = l.id_proyecto "
            + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
            + "LEFT JOIN cita ci ON ci.id_cita = l.id_cita ";

    public Levantamiento crear(Levantamiento l) throws SoptecException {
        String sql = "INSERT INTO levantamiento (fecha_visita, croquis, observaciones_tecnicas, activo, id_cita, id_proyecto) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_LEVANTAMIENTO");
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(l.getFechaVisita()));
            DaoSupport.setNullableString(ps, 2, l.getCroquis());
            DaoSupport.setNullableString(ps, 3, l.getObservacionesTecnicas());
            ps.setBoolean(4, l.isActivo());
            DaoSupport.setNullableInt(ps, 5, l.getIdCita());
            ps.setInt(6, l.getIdProyecto());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return obtenerPorId(keys.getInt(1));
                }
                throw new OperacionBDException(MODULO, "CREAR_LEVANTAMIENTO",
                        "El levantamiento se registró pero no se obtuvo su identificador.");
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "CREAR_LEVANTAMIENTO",
                    "No se pudo registrar el levantamiento del proyecto.", ex);
        }
    }

    public Levantamiento obtenerPorId(int idLevantamiento) throws SoptecException {
        String sql = SELECT_BASE + "WHERE l.id_levantamiento = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLevantamiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "CONSULTAR_POR_ID",
                    "Error al consultar el levantamiento.", ex);
        }
    }

    public List<Levantamiento> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = SELECT_BASE
                + (incluirInactivos ? "" : "WHERE l.activo = 1 ")
                + "ORDER BY l.fecha_visita DESC, l.id_levantamiento DESC";
        List<Levantamiento> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_LEVANTAMIENTOS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "LISTAR_LEVANTAMIENTOS",
                    "Error al listar los levantamientos.", ex);
        }
    }

    public List<Levantamiento> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE (p.nombre_proyecto LIKE ? OR c.nombre_cliente LIKE ? OR l.observaciones_tecnicas LIKE ?) "
                + (incluirInactivos ? "" : "AND l.activo = 1 ")
                + "ORDER BY l.fecha_visita DESC, l.id_levantamiento DESC";
        List<Levantamiento> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_LEVANTAMIENTOS");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "BUSCAR_LEVANTAMIENTOS",
                    "Error al buscar levantamientos.", ex);
        }
    }

    public boolean actualizar(Levantamiento l) throws SoptecException {
        String sql = "UPDATE levantamiento SET fecha_visita = ?, croquis = ?, observaciones_tecnicas = ?, "
                + "activo = ?, id_cita = ?, id_proyecto = ? WHERE id_levantamiento = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_LEVANTAMIENTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(l.getFechaVisita()));
            DaoSupport.setNullableString(ps, 2, l.getCroquis());
            DaoSupport.setNullableString(ps, 3, l.getObservacionesTecnicas());
            ps.setBoolean(4, l.isActivo());
            DaoSupport.setNullableInt(ps, 5, l.getIdCita());
            ps.setInt(6, l.getIdProyecto());
            ps.setInt(7, l.getIdLevantamiento());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "ACTUALIZAR_LEVANTAMIENTO",
                    "No se pudo actualizar el levantamiento.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idLevantamiento, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_LEVANTAMIENTO" : "ELIMINAR_LOGICO_LEVANTAMIENTO";
        String sql = "UPDATE levantamiento SET activo = ? WHERE id_levantamiento = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idLevantamiento);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", op,
                    "No se pudo cambiar el estado del levantamiento.", ex);
        }
    }

    public boolean eliminarFisico(int idLevantamiento) throws SoptecException {
        String sql = "DELETE FROM levantamiento WHERE id_levantamiento = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_LEVANTAMIENTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLevantamiento);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "LevantamientoDao", "ELIMINAR_FISICO_LEVANTAMIENTO",
                    "No se pudo eliminar el levantamiento definitivamente.", ex);
        }
    }

    private Levantamiento mapear(ResultSet rs) throws SQLException {
        Date f = rs.getDate("fecha_visita");
        int citaRaw = rs.getInt("id_cita");
        Integer idCita = rs.wasNull() ? null : citaRaw;
        return new Levantamiento(
                rs.getInt("id_levantamiento"),
                f != null ? f.toLocalDate() : null,
                rs.getString("croquis"),
                rs.getString("observaciones_tecnicas"),
                rs.getInt("id_proyecto"),
                idCita,
                rs.getBoolean("activo"),
                rs.getString("nombre_proyecto"),
                rs.getString("nombre_cliente"),
                rs.getString("fecha_hora_cita")
        );
    }
}
