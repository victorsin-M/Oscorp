package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla proveedor.
 */
public class ProveedorDao {

    private static final String MODULO = "Proveedores";

    public Proveedor crear(Proveedor p) throws SoptecException {
        String sql = "INSERT INTO proveedor (nombre_proveedor, telefono_proveedor, correo_proveedor, imagen, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombreProveedor());
            ps.setString(2, p.getTelefonoProveedor());
            ps.setString(3, p.getCorreoProveedor());
            DaoSupport.setNullableString(ps, 4, p.getImagen());
            ps.setBoolean(5, p.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setIdProveedor(keys.getInt(1));
                } else {
                    throw new OperacionBDException(MODULO, "CREAR_PROVEEDOR",
                            "El proveedor se registró pero no se obtuvo su identificador.");
                }
            }
            return p;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "CREAR_PROVEEDOR",
                    "No se pudo registrar el proveedor.", ex);
        }
    }

    public Proveedor obtenerPorId(int idProveedor) throws SoptecException {
        String sql = "SELECT id_proveedor, nombre_proveedor, telefono_proveedor, correo_proveedor, imagen, activo "
                + "FROM proveedor WHERE id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "CONSULTAR_POR_ID",
                    "Error al consultar el proveedor.", ex);
        }
    }

    public List<Proveedor> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT id_proveedor, nombre_proveedor, telefono_proveedor, correo_proveedor, imagen, activo "
                + "FROM proveedor "
                + (incluirInactivos ? "" : "WHERE activo = 1 ")
                + "ORDER BY nombre_proveedor ASC";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PROVEEDORES");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "LISTAR_PROVEEDORES",
                    "Error al obtener los proveedores.", ex);
        }
    }

    public List<Proveedor> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT id_proveedor, nombre_proveedor, telefono_proveedor, correo_proveedor, imagen, activo "
                + "FROM proveedor WHERE (nombre_proveedor LIKE ? OR correo_proveedor LIKE ? OR telefono_proveedor LIKE ?) "
                + (incluirInactivos ? "" : "AND activo = 1 ")
                + "ORDER BY nombre_proveedor ASC";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_PROVEEDORES");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "BUSCAR_PROVEEDORES",
                    "Error al buscar proveedores.", ex);
        }
    }

    public boolean actualizar(Proveedor p) throws SoptecException {
        String sql = "UPDATE proveedor SET nombre_proveedor = ?, telefono_proveedor = ?, correo_proveedor = ?, "
                + "imagen = ?, activo = ? WHERE id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProveedor());
            ps.setString(2, p.getTelefonoProveedor());
            ps.setString(3, p.getCorreoProveedor());
            DaoSupport.setNullableString(ps, 4, p.getImagen());
            ps.setBoolean(5, p.isActivo());
            ps.setInt(6, p.getIdProveedor());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "ACTUALIZAR_PROVEEDOR",
                    "No se pudo actualizar el proveedor.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idProveedor, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_PROVEEDOR" : "ELIMINAR_LOGICO_PROVEEDOR";
        String sql = "UPDATE proveedor SET activo = ? WHERE id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProveedor);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", op,
                    "No se pudo cambiar el estado del proveedor.", ex);
        }
    }

    public boolean eliminarFisico(int idProveedor) throws SoptecException {
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "ELIMINAR_FISICO_PROVEEDOR",
                    "No se pudo eliminar el proveedor definitivamente.", ex);
        }
    }

    public boolean existeDuplicado(String nombre, String correo, Integer excluirId) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE (LOWER(nombre_proveedor) = LOWER(?) OR LOWER(correo_proveedor) = LOWER(?)) "
                + (excluirId != null && excluirId > 0 ? "AND id_proveedor <> ?" : "");
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_DUPLICADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            if (excluirId != null && excluirId > 0) {
                ps.setInt(3, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "VERIFICAR_DUPLICADO",
                    "Error al validar duplicado de proveedor.", ex);
        }
    }

    public boolean tieneRelaciones(int idProveedor) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM material_proveedor WHERE id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProveedorDao", "VERIFICAR_RELACIONES",
                    "Error al verificar relaciones del proveedor.", ex);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id_proveedor"),
                rs.getString("nombre_proveedor"),
                rs.getString("telefono_proveedor"),
                rs.getString("correo_proveedor"),
                rs.getString("imagen"),
                rs.getBoolean("activo")
        );
    }
}
