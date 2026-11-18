package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Cita;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla cita (relacionada con cliente).
 */
public class CitaDao {

    private static final String MODULO = "Citas";

    public Cita crear(Cita c) throws SoptecException {
        String sql = "INSERT INTO cita (fecha_cita, hora_cita, estado_cita, observaciones, id_cliente, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_CITA");
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(c.getFechaCita()));
            ps.setTime(2, Time.valueOf(c.getHoraCita()));
            ps.setString(3, c.getEstadoCita());
            DaoSupport.setNullableString(ps, 4, c.getObservaciones());
            ps.setInt(5, c.getIdCliente());
            ps.setBoolean(6, c.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return obtenerPorId(keys.getInt(1));
                }
                throw new OperacionBDException(MODULO, "CREAR_CITA",
                        "La cita se registró pero no se obtuvo su identificador.");
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "CREAR_CITA",
                    "No se pudo agendar la cita.", ex);
        }
    }

    public Cita obtenerPorId(int idCita) throws SoptecException {
        String sql = "SELECT ci.id_cita, ci.fecha_cita, ci.hora_cita, ci.estado_cita, ci.observaciones, "
                + "ci.id_cliente, ci.activo, c.nombre_cliente, c.rfc "
                + "FROM cita ci "
                + "INNER JOIN cliente c ON c.id_cliente = ci.id_cliente "
                + "WHERE ci.id_cita = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "CONSULTAR_POR_ID",
                    "Error al consultar la cita.", ex);
        }
    }

    public List<Cita> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT ci.id_cita, ci.fecha_cita, ci.hora_cita, ci.estado_cita, ci.observaciones, "
                + "ci.id_cliente, ci.activo, c.nombre_cliente, c.rfc "
                + "FROM cita ci "
                + "INNER JOIN cliente c ON c.id_cliente = ci.id_cliente "
                + (incluirInactivos ? "" : "WHERE ci.activo = 1 ")
                + "ORDER BY ci.fecha_cita DESC, ci.hora_cita ASC";
        List<Cita> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_CITAS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "LISTAR_CITAS",
                    "Error al listar las citas.", ex);
        }
    }

    public List<Cita> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT ci.id_cita, ci.fecha_cita, ci.hora_cita, ci.estado_cita, ci.observaciones, "
                + "ci.id_cliente, ci.activo, c.nombre_cliente, c.rfc "
                + "FROM cita ci "
                + "INNER JOIN cliente c ON c.id_cliente = ci.id_cliente "
                + "WHERE (c.nombre_cliente LIKE ? OR c.rfc LIKE ? OR ci.estado_cita LIKE ? OR ci.observaciones LIKE ?) "
                + (incluirInactivos ? "" : "AND ci.activo = 1 ")
                + "ORDER BY ci.fecha_cita DESC, ci.hora_cita ASC";
        List<Cita> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_CITAS");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "BUSCAR_CITAS",
                    "Error al buscar citas.", ex);
        }
    }

    public boolean actualizar(Cita c) throws SoptecException {
        String sql = "UPDATE cita SET fecha_cita = ?, hora_cita = ?, estado_cita = ?, observaciones = ?, "
                + "id_cliente = ?, activo = ? WHERE id_cita = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_CITA");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(c.getFechaCita()));
            ps.setTime(2, Time.valueOf(c.getHoraCita()));
            ps.setString(3, c.getEstadoCita());
            DaoSupport.setNullableString(ps, 4, c.getObservaciones());
            ps.setInt(5, c.getIdCliente());
            ps.setBoolean(6, c.isActivo());
            ps.setInt(7, c.getIdCita());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "ACTUALIZAR_CITA",
                    "No se pudo actualizar la cita.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idCita, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_CITA" : "ELIMINAR_LOGICO_CITA";
        String sql = "UPDATE cita SET activo = ? WHERE id_cita = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", op,
                    "No se pudo cambiar el estado de la cita.", ex);
        }
    }

    public boolean eliminarFisico(int idCita) throws SoptecException {
        String sql = "DELETE FROM cita WHERE id_cita = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_CITA");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "ELIMINAR_FISICO_CITA",
                    "No se pudo eliminar la cita definitivamente.", ex);
        }
    }

    public boolean tieneLevantamientos(int idCita) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM levantamiento WHERE id_cita = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "CitaDao", "VERIFICAR_RELACIONES",
                    "Error al verificar levantamientos vinculados a la cita.", ex);
        }
    }

    private Cita mapear(ResultSet rs) throws SQLException {
        Date f = rs.getDate("fecha_cita");
        Time h = rs.getTime("hora_cita");
        return new Cita(
                rs.getInt("id_cita"),
                f != null ? f.toLocalDate() : null,
                h != null ? h.toLocalTime() : null,
                rs.getString("estado_cita"),
                rs.getString("observaciones"),
                rs.getInt("id_cliente"),
                rs.getBoolean("activo"),
                rs.getString("nombre_cliente"),
                rs.getString("rfc")
        );
    }
}
