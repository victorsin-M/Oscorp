package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla empleado.
 */
public class EmpleadoDao {

    private static final String MODULO = "Empleados";

    public Empleado crear(Empleado e) throws SoptecException {
        String sql = "INSERT INTO empleado (nombre, telefono, puesto, correo, imagen, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_EMPLEADO");
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTelefono());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getCorreo());
            DaoSupport.setNullableString(ps, 5, e.getImagen());
            ps.setBoolean(6, e.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setIdEmpleado(keys.getInt(1));
                } else {
                    throw new OperacionBDException(MODULO, "CREAR_EMPLEADO",
                            "El empleado se registró pero no se obtuvo su identificador.");
                }
            }
            return e;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "CREAR_EMPLEADO",
                    "No se pudo registrar el empleado.", ex);
        }
    }

    public Empleado obtenerPorId(int idEmpleado) throws SoptecException {
        String sql = "SELECT id_empleado, nombre, telefono, puesto, correo, imagen, activo "
                + "FROM empleado WHERE id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "CONSULTAR_POR_ID",
                    "Error al consultar el empleado.", ex);
        }
    }

    public List<Empleado> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT id_empleado, nombre, telefono, puesto, correo, imagen, activo FROM empleado "
                + (incluirInactivos ? "" : "WHERE activo = 1 ")
                + "ORDER BY nombre ASC";
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_EMPLEADOS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "LISTAR_EMPLEADOS",
                    "Error al obtener los empleados.", ex);
        }
    }

    public List<Empleado> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT id_empleado, nombre, telefono, puesto, correo, imagen, activo FROM empleado "
                + "WHERE (nombre LIKE ? OR puesto LIKE ? OR correo LIKE ? OR telefono LIKE ?) "
                + (incluirInactivos ? "" : "AND activo = 1 ")
                + "ORDER BY nombre ASC";
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_EMPLEADOS");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "BUSCAR_EMPLEADOS",
                    "Error al buscar empleados.", ex);
        }
    }

    public boolean actualizar(Empleado e) throws SoptecException {
        String sql = "UPDATE empleado SET nombre = ?, telefono = ?, puesto = ?, correo = ?, imagen = ?, activo = ? "
                + "WHERE id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_EMPLEADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTelefono());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getCorreo());
            DaoSupport.setNullableString(ps, 5, e.getImagen());
            ps.setBoolean(6, e.isActivo());
            ps.setInt(7, e.getIdEmpleado());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "ACTUALIZAR_EMPLEADO",
                    "No se pudo actualizar el empleado.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idEmpleado, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_EMPLEADO" : "ELIMINAR_LOGICO_EMPLEADO";
        String sql = "UPDATE empleado SET activo = ? WHERE id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", op,
                    "No se pudo cambiar el estado del empleado.", ex);
        }
    }

    public boolean eliminarFisico(int idEmpleado) throws SoptecException {
        String sql = "DELETE FROM empleado WHERE id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_EMPLEADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "ELIMINAR_FISICO_EMPLEADO",
                    "No se pudo eliminar el empleado definitivamente.", ex);
        }
    }

    public boolean existeDuplicado(String correo, Integer excluirId) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM empleado WHERE LOWER(correo) = LOWER(?) "
                + (excluirId != null && excluirId > 0 ? "AND id_empleado <> ?" : "");
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_DUPLICADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            if (excluirId != null && excluirId > 0) {
                ps.setInt(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "VERIFICAR_DUPLICADO",
                    "Error al validar duplicado de empleado.", ex);
        }
    }

    public boolean tieneRelaciones(int idEmpleado) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM proyecto_empleado WHERE id_empleado = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "EmpleadoDao", "VERIFICAR_RELACIONES",
                    "Error al verificar proyectos asociados al empleado.", ex);
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id_empleado"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("puesto"),
                rs.getString("correo"),
                rs.getString("imagen"),
                rs.getBoolean("activo")
        );
    }
}
