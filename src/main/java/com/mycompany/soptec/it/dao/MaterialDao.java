package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Material;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla material.
 */
public class MaterialDao {

    private static final String MODULO = "Materiales";

    public Material crear(Material m) throws SoptecException {
        String sql = "INSERT INTO material (descripcion_material, precio_costo, existencias, activo) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getDescripcionMaterial());
            ps.setBigDecimal(2, m.getPrecioCosto());
            ps.setInt(3, m.getExistencias());
            ps.setBoolean(4, m.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setIdMaterial(keys.getInt(1));
                } else {
                    throw new OperacionBDException(MODULO, "CREAR_MATERIAL",
                            "El material se registró pero no se obtuvo su identificador.");
                }
            }
            return m;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "CREAR_MATERIAL",
                    "No se pudo registrar el material.", ex);
        }
    }

    public Material obtenerPorId(int idMaterial) throws SoptecException {
        String sql = "SELECT id_material, descripcion_material, precio_costo, existencias, activo "
                + "FROM material WHERE id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "CONSULTAR_POR_ID",
                    "Error al consultar el material.", ex);
        }
    }

    public List<Material> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT id_material, descripcion_material, precio_costo, existencias, activo FROM material "
                + (incluirInactivos ? "" : "WHERE activo = 1 ")
                + "ORDER BY descripcion_material ASC";
        List<Material> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_MATERIALES");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "LISTAR_MATERIALES",
                    "Error al obtener los materiales.", ex);
        }
    }

    public List<Material> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT id_material, descripcion_material, precio_costo, existencias, activo FROM material "
                + "WHERE descripcion_material LIKE ? "
                + (incluirInactivos ? "" : "AND activo = 1 ")
                + "ORDER BY descripcion_material ASC";
        List<Material> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_MATERIALES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "BUSCAR_MATERIALES",
                    "Error al buscar materiales.", ex);
        }
    }

    public boolean actualizar(Material m) throws SoptecException {
        String sql = "UPDATE material SET descripcion_material = ?, precio_costo = ?, existencias = ?, activo = ? "
                + "WHERE id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getDescripcionMaterial());
            ps.setBigDecimal(2, m.getPrecioCosto());
            ps.setInt(3, m.getExistencias());
            ps.setBoolean(4, m.isActivo());
            ps.setInt(5, m.getIdMaterial());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "ACTUALIZAR_MATERIAL",
                    "No se pudo actualizar el material.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idMaterial, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_MATERIAL" : "ELIMINAR_LOGICO_MATERIAL";
        String sql = "UPDATE material SET activo = ? WHERE id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idMaterial);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", op,
                    "No se pudo cambiar el estado del material.", ex);
        }
    }

    public boolean eliminarFisico(int idMaterial) throws SoptecException {
        String sql = "DELETE FROM material WHERE id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "ELIMINAR_FISICO_MATERIAL",
                    "No se pudo eliminar el material definitivamente.", ex);
        }
    }

    public boolean existeDuplicado(String descripcion, Integer excluirId) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM material WHERE LOWER(descripcion_material) = LOWER(?) "
                + (excluirId != null && excluirId > 0 ? "AND id_material <> ?" : "");
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_DUPLICADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            if (excluirId != null && excluirId > 0) {
                ps.setInt(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "VERIFICAR_DUPLICADO",
                    "Error al validar duplicado de material.", ex);
        }
    }

    public boolean tieneRelaciones(int idMaterial) throws SoptecException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM material_proveedor WHERE id_material = ?) + "
                + "(SELECT COUNT(*) FROM orden_compra_material WHERE id_material = ?) + "
                + "(SELECT COUNT(*) FROM proyecto_material WHERE id_material = ?) AS total";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            ps.setInt(2, idMaterial);
            ps.setInt(3, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialDao", "VERIFICAR_RELACIONES",
                    "Error al verificar relaciones del material.", ex);
        }
    }

    private Material mapear(ResultSet rs) throws SQLException {
        return new Material(
                rs.getInt("id_material"),
                rs.getString("descripcion_material"),
                rs.getBigDecimal("precio_costo"),
                rs.getInt("existencias"),
                rs.getBoolean("activo")
        );
    }
}
