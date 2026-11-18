package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla proyecto (relacionado con cliente).
 */
public class ProyectoDao {

    private static final String MODULO = "Proyectos";

    public Proyecto crear(Proyecto p) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_PROYECTO")) {
            int id = crear(con, p);
            return obtenerPorId(id);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "CREAR_PROYECTO",
                    "No se pudo registrar el proyecto.", ex);
        }
    }

    public int crear(Connection con, Proyecto p) throws SQLException, OperacionBDException {
        String sql = "INSERT INTO proyecto (nombre_proyecto, fecha_inicio, fecha_estimada_fin, estado, costo_total, id_cliente, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombreProyecto());
            ps.setDate(2, Date.valueOf(p.getFechaInicio()));
            DaoSupport.setNullableDate(ps, 3, p.getFechaEstimadaFin());
            ps.setString(4, p.getEstado());
            ps.setBigDecimal(5, p.getCostoTotal());
            ps.setInt(6, p.getIdCliente());
            ps.setBoolean(7, p.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    p.setIdProyecto(id);
                    return id;
                }
                throw new OperacionBDException(MODULO, "CREAR_PROYECTO",
                        "El proyecto se registró pero no se obtuvo su identificador.");
            }
        }
    }

    /**
     * Transaccion multi-tabla: registra un proyecto y su levantamiento inicial.
     */
    public Proyecto crearConLevantamientoInicial(Proyecto proyecto, Levantamiento levantamiento, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "CREAR_PROYECTO_CON_LEVANTAMIENTO");
            con.setAutoCommit(false);

            int idProyecto = crear(con, proyecto);

            if (simularFallo) {
                throw new SQLException("Fallo simulado antes de insertar levantamiento para probar rollback.", "45000", 9998);
            }

            if (levantamiento != null) {
                String sqlLev = "INSERT INTO levantamiento (fecha_visita, croquis, observaciones_tecnicas, activo, id_cita, id_proyecto) "
                        + "VALUES (?, ?, ?, 1, ?, ?)";
                try (PreparedStatement psLev = con.prepareStatement(sqlLev)) {
                    psLev.setDate(1, Date.valueOf(levantamiento.getFechaVisita()));
                    DaoSupport.setNullableString(psLev, 2, levantamiento.getCroquis());
                    DaoSupport.setNullableString(psLev, 3, levantamiento.getObservacionesTecnicas());
                    DaoSupport.setNullableInt(psLev, 4, levantamiento.getIdCita());
                    psLev.setInt(5, idProyecto);
                    psLev.executeUpdate();
                }
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "ProyectoDao", "CREAR_PROYECTO_CON_LEVANTAMIENTO",
                    "Transaccion confirmada para proyecto ID=" + idProyecto);
            return obtenerPorId(idProyecto);
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_PROYECTO_CON_LEVANTAMIENTO");
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "CREAR_PROYECTO_CON_LEVANTAMIENTO",
                    "No se pudo completar el registro del proyecto y su levantamiento. Se revirtieron los cambios.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_PROYECTO_CON_LEVANTAMIENTO");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    public Proyecto obtenerPorId(int idProyecto) throws SoptecException {
        String sql = "SELECT p.id_proyecto, p.nombre_proyecto, p.fecha_inicio, p.fecha_estimada_fin, "
                + "p.estado, p.costo_total, p.id_cliente, p.activo, c.nombre_cliente, c.rfc "
                + "FROM proyecto p "
                + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
                + "WHERE p.id_proyecto = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "CONSULTAR_POR_ID",
                    "Error al consultar el proyecto.", ex);
        }
    }

    public List<Proyecto> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT p.id_proyecto, p.nombre_proyecto, p.fecha_inicio, p.fecha_estimada_fin, "
                + "p.estado, p.costo_total, p.id_cliente, p.activo, c.nombre_cliente, c.rfc "
                + "FROM proyecto p "
                + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
                + (incluirInactivos ? "" : "WHERE p.activo = 1 ")
                + "ORDER BY p.fecha_inicio DESC, p.id_proyecto DESC";
        List<Proyecto> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PROYECTOS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "LISTAR_PROYECTOS",
                    "Error al listar los proyectos.", ex);
        }
    }

    public List<Proyecto> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT p.id_proyecto, p.nombre_proyecto, p.fecha_inicio, p.fecha_estimada_fin, "
                + "p.estado, p.costo_total, p.id_cliente, p.activo, c.nombre_cliente, c.rfc "
                + "FROM proyecto p "
                + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
                + "WHERE (p.nombre_proyecto LIKE ? OR p.estado LIKE ? OR c.nombre_cliente LIKE ? OR c.rfc LIKE ?) "
                + (incluirInactivos ? "" : "AND p.activo = 1 ")
                + "ORDER BY p.fecha_inicio DESC, p.id_proyecto DESC";
        List<Proyecto> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_PROYECTOS");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "BUSCAR_PROYECTOS",
                    "Error al buscar proyectos.", ex);
        }
    }

    public boolean actualizar(Proyecto p) throws SoptecException {
        String sql = "UPDATE proyecto SET nombre_proyecto = ?, fecha_inicio = ?, fecha_estimada_fin = ?, "
                + "estado = ?, costo_total = ?, id_cliente = ?, activo = ? WHERE id_proyecto = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_PROYECTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProyecto());
            ps.setDate(2, Date.valueOf(p.getFechaInicio()));
            DaoSupport.setNullableDate(ps, 3, p.getFechaEstimadaFin());
            ps.setString(4, p.getEstado());
            ps.setBigDecimal(5, p.getCostoTotal());
            ps.setInt(6, p.getIdCliente());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getIdProyecto());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "ACTUALIZAR_PROYECTO",
                    "No se pudo actualizar el proyecto.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idProyecto, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_PROYECTO" : "ELIMINAR_LOGICO_PROYECTO";
        String sql = "UPDATE proyecto SET activo = ? WHERE id_proyecto = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProyecto);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", op,
                    "No se pudo cambiar el estado del proyecto.", ex);
        }
    }

    public boolean eliminarFisico(int idProyecto) throws SoptecException {
        String sql = "DELETE FROM proyecto WHERE id_proyecto = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_PROYECTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "ELIMINAR_FISICO_PROYECTO",
                    "No se pudo eliminar el proyecto definitivamente.", ex);
        }
    }

    public boolean tieneLevantamientos(int idProyecto, boolean soloActivos) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM levantamiento WHERE id_proyecto = ?"
                + (soloActivos ? " AND activo = 1" : "");
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_LEVANTAMIENTOS");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "VERIFICAR_LEVANTAMIENTOS",
                    "Error al verificar levantamientos del proyecto.", ex);
        }
    }

    public boolean tieneOtrasRelaciones(int idProyecto) throws SoptecException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM orden_compra WHERE id_proyecto = ?) + "
                + "(SELECT COUNT(*) FROM pago WHERE id_proyecto = ?) + "
                + "(SELECT COUNT(*) FROM proyecto_empleado WHERE id_proyecto = ?) + "
                + "(SELECT COUNT(*) FROM proyecto_material WHERE id_proyecto = ?) AS total";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idProyecto);
            ps.setInt(3, idProyecto);
            ps.setInt(4, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoDao", "VERIFICAR_RELACIONES",
                    "Error al verificar relaciones del proyecto.", ex);
        }
    }

    private Proyecto mapear(ResultSet rs) throws SQLException {
        Date fechaInicio = rs.getDate("fecha_inicio");
        Date fechaFin = rs.getDate("fecha_estimada_fin");
        return new Proyecto(
                rs.getInt("id_proyecto"),
                rs.getString("nombre_proyecto"),
                fechaInicio != null ? fechaInicio.toLocalDate() : null,
                fechaFin != null ? fechaFin.toLocalDate() : null,
                rs.getString("estado"),
                rs.getBigDecimal("costo_total"),
                rs.getInt("id_cliente"),
                rs.getBoolean("activo"),
                rs.getString("nombre_cliente"),
                rs.getString("rfc")
        );
    }
}
