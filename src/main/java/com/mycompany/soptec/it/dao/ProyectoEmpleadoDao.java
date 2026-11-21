package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.ProyectoEmpleado;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla puente M:N proyecto_empleado (Proyecto <-> Empleado).
 */
public class ProyectoEmpleadoDao {

    private static final String MODULO = "ProyectoEmpleado";

    private static final String SELECT_BASE =
            "SELECT pe.id_proyecto, pe.id_empleado, p.nombre_proyecto, p.estado AS estado_proyecto, "
            + "c.nombre_cliente, e.nombre AS nombre_empleado, e.puesto, e.correo "
            + "FROM proyecto_empleado pe "
            + "INNER JOIN proyecto p ON p.id_proyecto = pe.id_proyecto "
            + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente "
            + "INNER JOIN empleado e ON e.id_empleado = pe.id_empleado ";

    public void asignar(int idProyecto, int idEmpleado) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "ASIGNAR_EMPLEADO")) {
            asignar(con, idProyecto, idEmpleado);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "ASIGNAR_EMPLEADO",
                    "No se pudo asignar el empleado al proyecto.", ex);
        }
    }

    public void asignar(Connection con, int idProyecto, int idEmpleado) throws SQLException {
        String sql = "INSERT INTO proyecto_empleado (id_proyecto, id_empleado) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public boolean existe(int idProyecto, int idEmpleado) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM proyecto_empleado WHERE id_proyecto = ? AND id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_ASIGNACION");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "VERIFICAR_ASIGNACION",
                    "Error al verificar la asignación del empleado.", ex);
        }
    }

    public List<ProyectoEmpleado> listarTodos() throws SoptecException {
        String sql = SELECT_BASE + "ORDER BY p.id_proyecto DESC, e.nombre ASC";
        List<ProyectoEmpleado> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_ASIGNACIONES");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "LISTAR_ASIGNACIONES",
                    "Error al listar las asignaciones proyecto-empleado.", ex);
        }
    }

    public List<ProyectoEmpleado> listarPorProyecto(int idProyecto) throws SoptecException {
        String sql = SELECT_BASE + "WHERE pe.id_proyecto = ? ORDER BY e.nombre ASC";
        List<ProyectoEmpleado> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_POR_PROYECTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "LISTAR_POR_PROYECTO",
                    "Error al consultar el personal asignado al proyecto.", ex);
        }
    }

    public List<ProyectoEmpleado> buscar(String criterio) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE p.nombre_proyecto LIKE ? OR c.nombre_cliente LIKE ? OR e.nombre LIKE ? OR e.puesto LIKE ? "
                + "ORDER BY p.id_proyecto DESC, e.nombre ASC";
        List<ProyectoEmpleado> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_ASIGNACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "BUSCAR_ASIGNACIONES",
                    "Error al buscar asignaciones proyecto-empleado.", ex);
        }
    }

    public boolean actualizarAsignacion(int idProyectoOrig, int idEmpleadoOrig,
            int idProyectoNuevo, int idEmpleadoNuevo) throws SoptecException {
        String sql = "UPDATE proyecto_empleado SET id_proyecto = ?, id_empleado = ? "
                + "WHERE id_proyecto = ? AND id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_ASIGNACION");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyectoNuevo);
            ps.setInt(2, idEmpleadoNuevo);
            ps.setInt(3, idProyectoOrig);
            ps.setInt(4, idEmpleadoOrig);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "ACTUALIZAR_ASIGNACION",
                    "No se pudo actualizar la asignación proyecto-empleado.", ex);
        }
    }

    public boolean eliminar(int idProyecto, int idEmpleado) throws SoptecException {
        String sql = "DELETE FROM proyecto_empleado WHERE id_proyecto = ? AND id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_ASIGNACION");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "ELIMINAR_ASIGNACION",
                    "No se pudo desvincular al empleado del proyecto.", ex);
        }
    }

    /**
     * Reemplaza atomica y conjuntamente el equipo asignado a un proyecto.
     */
    public void reemplazarEmpleadosDeProyecto(int idProyecto, List<Integer> idsEmpleados, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "SINCRONIZAR_EMPLEADOS_PROYECTO");
            con.setAutoCommit(false);

            try (PreparedStatement psDel = con.prepareStatement(
                    "DELETE FROM proyecto_empleado WHERE id_proyecto = ?")) {
                psDel.setInt(1, idProyecto);
                psDel.executeUpdate();
            }

            if (simularFallo) {
                throw new SQLException("Fallo simulado al sincronizar empleados del proyecto.", "45000", 9991);
            }

            if (idsEmpleados != null) {
                for (Integer idEmp : idsEmpleados) {
                    if (idEmp != null && idEmp > 0) {
                        asignar(con, idProyecto, idEmp);
                    }
                }
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "ProyectoEmpleadoDao", "SINCRONIZAR_EMPLEADOS_PROYECTO",
                    "Equipo sincronizado para proyecto ID=" + idProyecto);
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "SINCRONIZAR_EMPLEADOS_PROYECTO");
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoEmpleadoDao", "SINCRONIZAR_EMPLEADOS_PROYECTO",
                    "No se pudo actualizar el equipo del proyecto. Se revirtieron los cambios.", ex);
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    private ProyectoEmpleado mapear(ResultSet rs) throws SQLException {
        return new ProyectoEmpleado(
                rs.getInt("id_proyecto"),
                rs.getInt("id_empleado"),
                rs.getString("nombre_proyecto"),
                rs.getString("estado_proyecto"),
                rs.getString("nombre_cliente"),
                rs.getString("nombre_empleado"),
                rs.getString("puesto"),
                rs.getString("correo")
        );
    }
}
