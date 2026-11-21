package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para las partidas de la tabla puente M:N orden_compra_material.
 */
public class OrdenCompraMaterialDao {

    private static final String MODULO = "OrdenesCompra";

    public void insertar(Connection con, int idOrdenCompra, int idMaterial, BigDecimal cantidad) throws SQLException {
        String sql = "INSERT INTO orden_compra_material (id_orden_compra, id_material, cantidad) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE cantidad = cantidad + VALUES(cantidad)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            ps.setInt(2, idMaterial);
            ps.setBigDecimal(3, cantidad);
            ps.executeUpdate();
        }
    }

    public void eliminarPorOrden(Connection con, int idOrdenCompra) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM orden_compra_material WHERE id_orden_compra = ?")) {
            ps.setInt(1, idOrdenCompra);
            ps.executeUpdate();
        }
    }

    public List<OrdenCompraMaterial> listarPorOrden(int idOrdenCompra) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PARTIDAS_ORDEN")) {
            return listarPorOrden(con, idOrdenCompra);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraMaterialDao", "LISTAR_PARTIDAS_ORDEN",
                    "Error al consultar las partidas de la orden de compra.", ex);
        }
    }

    public List<OrdenCompraMaterial> listarPorOrden(Connection con, int idOrdenCompra) throws SQLException {
        String sql = "SELECT ocm.id_orden_compra, ocm.id_material, ocm.cantidad, "
                + "m.descripcion_material, m.precio_costo "
                + "FROM orden_compra_material ocm "
                + "INNER JOIN material m ON m.id_material = ocm.id_material "
                + "WHERE ocm.id_orden_compra = ? "
                + "ORDER BY m.descripcion_material ASC";
        List<OrdenCompraMaterial> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OrdenCompraMaterial(
                            rs.getInt("id_orden_compra"),
                            rs.getInt("id_material"),
                            rs.getBigDecimal("cantidad"),
                            rs.getString("descripcion_material"),
                            rs.getBigDecimal("precio_costo")
                    ));
                }
            }
        }
        return lista;
    }

    public BigDecimal calcularTotalOrden(Connection con, int idOrdenCompra) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ocm.cantidad * m.precio_costo), 0) "
                + "FROM orden_compra_material ocm "
                + "INNER JOIN material m ON m.id_material = ocm.id_material "
                + "WHERE ocm.id_orden_compra = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return total != null ? total : BigDecimal.ZERO;
                }
                return BigDecimal.ZERO;
            }
        }
    }
}
