package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.MaterialProveedor;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla puente M:N material_proveedor (Material <-> Proveedor).
 */
public class MaterialProveedorDao {

    private static final String MODULO = "MaterialProveedor";

    private static final String SELECT_BASE =
            "SELECT mp.id_material, mp.id_proveedor, "
            + "m.descripcion_material, m.precio_costo, m.existencias, "
            + "pr.nombre_proveedor, pr.telefono_proveedor, pr.correo_proveedor "
            + "FROM material_proveedor mp "
            + "INNER JOIN material m ON m.id_material = mp.id_material "
            + "INNER JOIN proveedor pr ON pr.id_proveedor = mp.id_proveedor ";

    public void asociar(int idMaterial, int idProveedor) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "ASOCIAR_MATERIAL_PROVEEDOR")) {
            asociar(con, idMaterial, idProveedor);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "ASOCIAR_MATERIAL_PROVEEDOR",
                    "No se pudo asociar el proveedor al material.", ex);
        }
    }

    public void asociar(Connection con, int idMaterial, int idProveedor) throws SQLException {
        String sql = "INSERT INTO material_proveedor (id_material, id_proveedor) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            ps.setInt(2, idProveedor);
            ps.executeUpdate();
        }
    }

    public boolean existe(int idMaterial, int idProveedor) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM material_proveedor WHERE id_material = ? AND id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_ASOCIACION");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            ps.setInt(2, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "VERIFICAR_ASOCIACION",
                    "Error al verificar la relación material-proveedor.", ex);
        }
    }

    public List<MaterialProveedor> listarTodos() throws SoptecException {
        String sql = SELECT_BASE + "ORDER BY m.descripcion_material ASC, pr.nombre_proveedor ASC";
        List<MaterialProveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_MATERIAL_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "LISTAR_MATERIAL_PROVEEDOR",
                    "Error al listar las relaciones material-proveedor.", ex);
        }
    }

    public List<MaterialProveedor> listarPorMaterial(int idMaterial) throws SoptecException {
        String sql = SELECT_BASE + "WHERE mp.id_material = ? ORDER BY pr.nombre_proveedor ASC";
        List<MaterialProveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_POR_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "LISTAR_POR_MATERIAL",
                    "Error al listar proveedores del material.", ex);
        }
    }

    public List<MaterialProveedor> listarPorProveedor(int idProveedor) throws SoptecException {
        String sql = SELECT_BASE + "WHERE mp.id_proveedor = ? ORDER BY m.descripcion_material ASC";
        List<MaterialProveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_POR_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "LISTAR_POR_PROVEEDOR",
                    "Error al listar materiales del proveedor.", ex);
        }
    }

    public List<MaterialProveedor> buscar(String criterio) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE m.descripcion_material LIKE ? OR pr.nombre_proveedor LIKE ? OR pr.correo_proveedor LIKE ? "
                + "ORDER BY m.descripcion_material ASC, pr.nombre_proveedor ASC";
        List<MaterialProveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_MATERIAL_PROVEEDOR");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "BUSCAR_MATERIAL_PROVEEDOR",
                    "Error al buscar relaciones material-proveedor.", ex);
        }
    }

    public boolean actualizarAsociacion(int idMatOrig, int idProvOrig, int idMatNuevo, int idProvNuevo)
            throws SoptecException {
        String sql = "UPDATE material_proveedor SET id_material = ?, id_proveedor = ? "
                + "WHERE id_material = ? AND id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_MATERIAL_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMatNuevo);
            ps.setInt(2, idProvNuevo);
            ps.setInt(3, idMatOrig);
            ps.setInt(4, idProvOrig);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "ACTUALIZAR_MATERIAL_PROVEEDOR",
                    "No se pudo actualizar la relación material-proveedor.", ex);
        }
    }

    public boolean eliminar(int idMaterial, int idProveedor) throws SoptecException {
        String sql = "DELETE FROM material_proveedor WHERE id_material = ? AND id_proveedor = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_MATERIAL_PROVEEDOR");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            ps.setInt(2, idProveedor);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "ELIMINAR_MATERIAL_PROVEEDOR",
                    "No se pudo eliminar la relación material-proveedor.", ex);
        }
    }

    /**
     * Sincroniza de forma atomica el conjunto de proveedores asociados a un material.
     */
    public void sincronizarProveedoresDeMaterial(int idMaterial, List<Integer> idsProveedores, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "SINCRONIZAR_PROVEEDORES");
            con.setAutoCommit(false);

            try (PreparedStatement psDel = con.prepareStatement(
                    "DELETE FROM material_proveedor WHERE id_material = ?")) {
                psDel.setInt(1, idMaterial);
                psDel.executeUpdate();
            }

            if (simularFallo) {
                throw new SQLException("Fallo simulado al sincronizar proveedores del material.", "45000", 9993);
            }

            if (idsProveedores != null) {
                for (Integer idProv : idsProveedores) {
                    if (idProv != null && idProv > 0) {
                        asociar(con, idMaterial, idProv);
                    }
                }
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "MaterialProveedorDao", "SINCRONIZAR_PROVEEDORES",
                    "Proveedores sincronizados para material ID=" + idMaterial);
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "SINCRONIZAR_PROVEEDORES");
            throw DaoSupport.traducirYRegistrar(MODULO, "MaterialProveedorDao", "SINCRONIZAR_PROVEEDORES",
                    "No se pudo guardar la lista de proveedores del material. Se revirtieron los cambios.", ex);
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    private MaterialProveedor mapear(ResultSet rs) throws SQLException {
        return new MaterialProveedor(
                rs.getInt("id_material"),
                rs.getInt("id_proveedor"),
                rs.getString("descripcion_material"),
                rs.getBigDecimal("precio_costo"),
                rs.getInt("existencias"),
                rs.getString("nombre_proveedor"),
                rs.getString("telefono_proveedor"),
                rs.getString("correo_proveedor")
        );
    }
}
