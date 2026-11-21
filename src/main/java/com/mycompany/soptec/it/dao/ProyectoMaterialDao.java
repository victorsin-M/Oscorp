package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla puente M:N proyecto_material (Proyecto <-> Material con cantidad y control de stock).
 */
public class ProyectoMaterialDao {

    private static final String MODULO = "ProyectoMaterial";

    private static final String SELECT_BASE =
            "SELECT pm.id_proyecto, pm.id_material, pm.cantidad, "
            + "p.nombre_proyecto, m.descripcion_material, m.precio_costo, m.existencias "
            + "FROM proyecto_material pm "
            + "INNER JOIN proyecto p ON p.id_proyecto = pm.id_proyecto "
            + "INNER JOIN material m ON m.id_material = pm.id_material ";

    public void asignar(int idProyecto, int idMaterial, BigDecimal cantidad) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "ASIGNAR_MATERIAL")) {
            asignar(con, idProyecto, idMaterial, cantidad);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "ASIGNAR_MATERIAL",
                    "No se pudo asignar el material al proyecto.", ex);
        }
    }

    public void asignar(Connection con, int idProyecto, int idMaterial, BigDecimal cantidad) throws SQLException {
        String sql = "INSERT INTO proyecto_material (id_proyecto, id_material, cantidad) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE cantidad = cantidad + VALUES(cantidad)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idMaterial);
            ps.setBigDecimal(3, cantidad);
            ps.executeUpdate();
        }
    }

    /**
     * Operacion transaccional: descuenta existencias en material y registra/acumula en proyecto_material.
     * Si falla cualquier paso, ejecuta rollback completo.
     */
    public void asignarConDescuentoStock(int idProyecto, int idMaterial, BigDecimal cantidad, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "ASIGNAR_MATERIAL_CON_STOCK");
            con.setAutoCommit(false);

            int descontar = cantidad.intValue();
            int existenciasActuales = obtenerExistenciasParaActualizar(con, idMaterial);
            if (existenciasActuales < descontar) {
                throw new ValidacionException(MODULO, "ASIGNAR_MATERIAL_CON_STOCK",
                        "No hay existencias suficientes del material seleccionado (disponible: "
                                + existenciasActuales + ", solicitado: " + descontar + ").");
            }

            try (PreparedStatement psStock = con.prepareStatement(
                    "UPDATE material SET existencias = existencias - ? WHERE id_material = ?")) {
                psStock.setInt(1, descontar);
                psStock.setInt(2, idMaterial);
                psStock.executeUpdate();
            }

            if (simularFallo) {
                throw new SQLException("Fallo simulado tras descontar stock para validar rollback.", "45000", 9992);
            }

            asignar(con, idProyecto, idMaterial, cantidad);

            con.commit();
            AppLogger.registrarInfo(MODULO, "ProyectoMaterialDao", "ASIGNAR_MATERIAL_CON_STOCK",
                    "Material ID=" + idMaterial + " asignado a proyecto ID=" + idProyecto + " (cant=" + cantidad + ")");
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ASIGNAR_MATERIAL_CON_STOCK");
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "ASIGNAR_MATERIAL_CON_STOCK",
                    "No se pudo asignar el material al proyecto. Se revirtieron los cambios de inventario.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ASIGNAR_MATERIAL_CON_STOCK");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    public ProyectoMaterial obtener(int idProyecto, int idMaterial) throws SoptecException {
        String sql = SELECT_BASE + "WHERE pm.id_proyecto = ? AND pm.id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_PROYECTO_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "CONSULTAR_PROYECTO_MATERIAL",
                    "Error al consultar el material asignado al proyecto.", ex);
        }
    }

    public List<ProyectoMaterial> listarTodos() throws SoptecException {
        String sql = SELECT_BASE + "ORDER BY p.id_proyecto DESC, m.descripcion_material ASC";
        List<ProyectoMaterial> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PROYECTO_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "LISTAR_PROYECTO_MATERIAL",
                    "Error al listar materiales asignados a proyectos.", ex);
        }
    }

    public List<ProyectoMaterial> listarPorProyecto(int idProyecto) throws SoptecException {
        String sql = SELECT_BASE + "WHERE pm.id_proyecto = ? ORDER BY m.descripcion_material ASC";
        List<ProyectoMaterial> lista = new ArrayList<>();
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
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "LISTAR_POR_PROYECTO",
                    "Error al listar los materiales del proyecto.", ex);
        }
    }

    public List<ProyectoMaterial> buscar(String criterio) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE p.nombre_proyecto LIKE ? OR m.descripcion_material LIKE ? "
                + "ORDER BY p.id_proyecto DESC, m.descripcion_material ASC";
        List<ProyectoMaterial> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_PROYECTO_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "BUSCAR_PROYECTO_MATERIAL",
                    "Error al buscar materiales asignados a proyectos.", ex);
        }
    }

    public boolean actualizarCantidad(int idProyecto, int idMaterial, BigDecimal nuevaCantidad) throws SoptecException {
        String sql = "UPDATE proyecto_material SET cantidad = ? WHERE id_proyecto = ? AND id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_CANTIDAD");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevaCantidad);
            ps.setInt(2, idProyecto);
            ps.setInt(3, idMaterial);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "ACTUALIZAR_CANTIDAD",
                    "No se pudo actualizar la cantidad del material en el proyecto.", ex);
        }
    }

    public boolean eliminar(int idProyecto, int idMaterial) throws SoptecException {
        String sql = "DELETE FROM proyecto_material WHERE id_proyecto = ? AND id_material = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_PROYECTO_MATERIAL");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idMaterial);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "ELIMINAR_PROYECTO_MATERIAL",
                    "No se pudo quitar el material del proyecto.", ex);
        }
    }

    /**
     * Elimina el material del proyecto y devuelve las unidades al stock de forma atomica.
     */
    public boolean eliminarConReintegroStock(int idProyecto, int idMaterial) throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "ELIMINAR_CON_REINTEGRO");
            con.setAutoCommit(false);

            BigDecimal cantActual = null;
            try (PreparedStatement psGet = con.prepareStatement(
                    "SELECT cantidad FROM proyecto_material WHERE id_proyecto = ? AND id_material = ? FOR UPDATE")) {
                psGet.setInt(1, idProyecto);
                psGet.setInt(2, idMaterial);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        cantActual = rs.getBigDecimal(1);
                    }
                }
            }
            if (cantActual == null) {
                con.rollback();
                return false;
            }

            try (PreparedStatement psDel = con.prepareStatement(
                    "DELETE FROM proyecto_material WHERE id_proyecto = ? AND id_material = ?")) {
                psDel.setInt(1, idProyecto);
                psDel.setInt(2, idMaterial);
                psDel.executeUpdate();
            }

            try (PreparedStatement psStock = con.prepareStatement(
                    "UPDATE material SET existencias = existencias + ? WHERE id_material = ?")) {
                psStock.setInt(1, cantActual.intValue());
                psStock.setInt(2, idMaterial);
                psStock.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ELIMINAR_CON_REINTEGRO");
            throw DaoSupport.traducirYRegistrar(MODULO, "ProyectoMaterialDao", "ELIMINAR_CON_REINTEGRO",
                    "No se pudo retirar el material del proyecto. Se revirtieron los cambios.", ex);
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    private int obtenerExistenciasParaActualizar(Connection con, int idMaterial) throws SQLException, ValidacionException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT existencias FROM material WHERE id_material = ? FOR UPDATE")) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new ValidacionException(MODULO, "ASIGNAR_MATERIAL_CON_STOCK",
                        "El material seleccionado ya no existe en el catálogo.");
            }
        }
    }

    private ProyectoMaterial mapear(ResultSet rs) throws SQLException {
        return new ProyectoMaterial(
                rs.getInt("id_proyecto"),
                rs.getInt("id_material"),
                rs.getBigDecimal("cantidad"),
                rs.getString("nombre_proyecto"),
                rs.getString("descripcion_material"),
                rs.getBigDecimal("precio_costo"),
                rs.getInt("existencias")
        );
    }
}
