package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.OrdenCompra;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO transaccional para orden_compra y sus partidas M:N orden_compra_material.
 */
public class OrdenCompraDao {

    private static final String MODULO = "OrdenesCompra";
    private final OrdenCompraMaterialDao partidaDao = new OrdenCompraMaterialDao();

    private static final String SELECT_BASE =
            "SELECT oc.id_orden_compra, oc.id_proyecto, oc.id_proveedor, oc.fecha_orden, oc.total_compra, "
            + "oc.tiempo_entrega, oc.estado_orden, oc.activo, "
            + "p.nombre_proyecto, pr.nombre_proveedor, "
            + "(SELECT COUNT(*) FROM orden_compra_material ocm WHERE ocm.id_orden_compra = oc.id_orden_compra) AS total_partidas "
            + "FROM orden_compra oc "
            + "INNER JOIN proyecto p ON p.id_proyecto = oc.id_proyecto "
            + "LEFT JOIN proveedor pr ON pr.id_proveedor = oc.id_proveedor ";

    /**
     * Registra la cabecera de orden_compra y todas sus partidas (orden_compra_material)
     * en una sola transaccion. Si falla cualquier paso, ejecuta rollback completo.
     */
    public OrdenCompra crearConPartidas(OrdenCompra orden, List<OrdenCompraMaterial> partidas, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "CREAR_ORDEN_COMPRA");
            con.setAutoCommit(false);

            String sql = "INSERT INTO orden_compra "
                    + "(id_proyecto, id_proveedor, fecha_orden, total_compra, tiempo_entrega, estado_orden, activo) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            int idOrden;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orden.getIdProyecto());
                DaoSupport.setNullableInt(ps, 2, orden.getIdProveedor());
                ps.setDate(3, Date.valueOf(orden.getFechaOrden()));
                ps.setBigDecimal(4, BigDecimal.ZERO);
                DaoSupport.setNullableString(ps, 5, orden.getTiempoEntrega());
                ps.setString(6, orden.getEstadoOrden());
                ps.setBoolean(7, orden.isActivo());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        idOrden = keys.getInt(1);
                        orden.setIdOrdenCompra(idOrden);
                    } else {
                        throw new OperacionBDException(MODULO, "CREAR_ORDEN_COMPRA",
                                "La orden se registró pero no se obtuvo su identificador.");
                    }
                }
            }

            if (simularFallo) {
                throw new SQLException("Fallo simulado al insertar partidas de la orden de compra.", "45000", 9994);
            }

            for (OrdenCompraMaterial item : partidas) {
                partidaDao.insertar(con, idOrden, item.getIdMaterial(), item.getCantidad());
            }

            BigDecimal totalCalculado = partidaDao.calcularTotalOrden(con, idOrden);
            try (PreparedStatement psTot = con.prepareStatement(
                    "UPDATE orden_compra SET total_compra = ? WHERE id_orden_compra = ?")) {
                psTot.setBigDecimal(1, totalCalculado);
                psTot.setInt(2, idOrden);
                psTot.executeUpdate();
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "OrdenCompraDao", "CREAR_ORDEN_COMPRA",
                    "Orden de compra creada ID=" + idOrden + " con " + partidas.size() + " partidas, total=" + totalCalculado);
            return obtenerPorId(idOrden);
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_ORDEN_COMPRA");
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "CREAR_ORDEN_COMPRA",
                    "No se pudo registrar la orden de compra y sus materiales. Se revirtieron los cambios.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_ORDEN_COMPRA");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    /**
     * Actualiza la orden de compra y sincroniza sus partidas M:N de manera atomica.
     */
    public OrdenCompra actualizarConPartidas(OrdenCompra orden, List<OrdenCompraMaterial> partidas)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_ORDEN_COMPRA");
            con.setAutoCommit(false);

            String sql = "UPDATE orden_compra SET id_proyecto = ?, id_proveedor = ?, fecha_orden = ?, "
                    + "tiempo_entrega = ?, estado_orden = ?, activo = ? WHERE id_orden_compra = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, orden.getIdProyecto());
                DaoSupport.setNullableInt(ps, 2, orden.getIdProveedor());
                ps.setDate(3, Date.valueOf(orden.getFechaOrden()));
                DaoSupport.setNullableString(ps, 4, orden.getTiempoEntrega());
                ps.setString(5, orden.getEstadoOrden());
                ps.setBoolean(6, orden.isActivo());
                ps.setInt(7, orden.getIdOrdenCompra());
                if (ps.executeUpdate() == 0) {
                    throw new ValidacionException(MODULO, "ACTUALIZAR_ORDEN_COMPRA",
                            "La orden de compra ya no existe en la base de datos.");
                }
            }

            partidaDao.eliminarPorOrden(con, orden.getIdOrdenCompra());
            for (OrdenCompraMaterial item : partidas) {
                partidaDao.insertar(con, orden.getIdOrdenCompra(), item.getIdMaterial(), item.getCantidad());
            }

            BigDecimal totalCalculado = partidaDao.calcularTotalOrden(con, orden.getIdOrdenCompra());
            try (PreparedStatement psTot = con.prepareStatement(
                    "UPDATE orden_compra SET total_compra = ? WHERE id_orden_compra = ?")) {
                psTot.setBigDecimal(1, totalCalculado);
                psTot.setInt(2, orden.getIdOrdenCompra());
                psTot.executeUpdate();
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "OrdenCompraDao", "ACTUALIZAR_ORDEN_COMPRA",
                    "Orden de compra actualizada ID=" + orden.getIdOrdenCompra() + " total=" + totalCalculado);
            return obtenerPorId(orden.getIdOrdenCompra());
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ACTUALIZAR_ORDEN_COMPRA");
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "ACTUALIZAR_ORDEN_COMPRA",
                    "No se pudo actualizar la orden de compra. Se revirtieron los cambios.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ACTUALIZAR_ORDEN_COMPRA");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    /**
     * Marca la orden como Recibida e incrementa las existencias de cada material en una sola transaccion.
     */
    public OrdenCompra recibirOrden(int idOrdenCompra, boolean simularFallo) throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "RECIBIR_ORDEN_COMPRA");
            con.setAutoCommit(false);

            String estadoActual = null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado_orden FROM orden_compra WHERE id_orden_compra = ? FOR UPDATE")) {
                ps.setInt(1, idOrdenCompra);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        estadoActual = rs.getString(1);
                    }
                }
            }
            if (estadoActual == null) {
                throw new ValidacionException(MODULO, "RECIBIR_ORDEN_COMPRA", "La orden de compra no existe.");
            }
            if ("Recibida".equalsIgnoreCase(estadoActual)) {
                throw new ValidacionException(MODULO, "RECIBIR_ORDEN_COMPRA",
                        "Esta orden de compra ya fue recibida anteriormente.");
            }
            if ("Cancelada".equalsIgnoreCase(estadoActual)) {
                throw new ValidacionException(MODULO, "RECIBIR_ORDEN_COMPRA",
                        "No se puede recibir una orden de compra cancelada.");
            }

            List<OrdenCompraMaterial> partidas = partidaDao.listarPorOrden(con, idOrdenCompra);
            if (partidas.isEmpty()) {
                throw new ValidacionException(MODULO, "RECIBIR_ORDEN_COMPRA",
                        "La orden de compra no tiene partidas de materiales.");
            }

            try (PreparedStatement psStock = con.prepareStatement(
                    "UPDATE material SET existencias = existencias + ? WHERE id_material = ?")) {
                for (OrdenCompraMaterial p : partidas) {
                    psStock.setInt(1, p.getCantidad().intValue());
                    psStock.setInt(2, p.getIdMaterial());
                    psStock.executeUpdate();
                }
            }

            if (simularFallo) {
                throw new SQLException("Fallo simulado tras actualizar stock al recibir orden.", "45000", 9995);
            }

            try (PreparedStatement psEst = con.prepareStatement(
                    "UPDATE orden_compra SET estado_orden = 'Recibida' WHERE id_orden_compra = ?")) {
                psEst.setInt(1, idOrdenCompra);
                psEst.executeUpdate();
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "OrdenCompraDao", "RECIBIR_ORDEN_COMPRA",
                    "Orden recibida ID=" + idOrdenCompra + " y stock actualizado para " + partidas.size() + " materiales");
            return obtenerPorId(idOrdenCompra);
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "RECIBIR_ORDEN_COMPRA");
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "RECIBIR_ORDEN_COMPRA",
                    "No se pudo registrar la recepción de la orden. El inventario no sufrió cambios.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "RECIBIR_ORDEN_COMPRA");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    public boolean actualizarEstado(int idOrdenCompra, String nuevoEstado) throws SoptecException {
        String sql = "UPDATE orden_compra SET estado_orden = ? WHERE id_orden_compra = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_ESTADO_ORDEN");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idOrdenCompra);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "ACTUALIZAR_ESTADO_ORDEN",
                    "No se pudo actualizar el estado de la orden de compra.", ex);
        }
    }

    public OrdenCompra obtenerPorId(int idOrdenCompra) throws SoptecException {
        String sql = SELECT_BASE + "WHERE oc.id_orden_compra = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_ORDEN_COMPRA");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                OrdenCompra oc = mapear(rs);
                oc.setPartidas(partidaDao.listarPorOrden(con, idOrdenCompra));
                return oc;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "CONSULTAR_ORDEN_COMPRA",
                    "Error al consultar la orden de compra.", ex);
        }
    }

    public List<OrdenCompra> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = SELECT_BASE
                + (incluirInactivos ? "" : "WHERE oc.activo = 1 ")
                + "ORDER BY oc.fecha_orden DESC, oc.id_orden_compra DESC";
        List<OrdenCompra> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_ORDENES_COMPRA");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "LISTAR_ORDENES_COMPRA",
                    "Error al listar las órdenes de compra.", ex);
        }
    }

    public List<OrdenCompra> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE (p.nombre_proyecto LIKE ? OR IFNULL(pr.nombre_proveedor, '') LIKE ? "
                + "OR IFNULL(oc.tiempo_entrega, '') LIKE ? OR oc.estado_orden LIKE ?) "
                + (incluirInactivos ? "" : "AND oc.activo = 1 ")
                + "ORDER BY oc.fecha_orden DESC, oc.id_orden_compra DESC";
        List<OrdenCompra> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_ORDENES_COMPRA");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "BUSCAR_ORDENES_COMPRA",
                    "Error al buscar órdenes de compra.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idOrdenCompra, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_ORDEN_COMPRA" : "ELIMINAR_LOGICO_ORDEN_COMPRA";
        String sql = "UPDATE orden_compra SET activo = ? WHERE id_orden_compra = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idOrdenCompra);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", op,
                    "No se pudo cambiar el estado de la orden de compra.", ex);
        }
    }

    public boolean eliminarFisico(int idOrdenCompra) throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_ORDEN_COMPRA");
            con.setAutoCommit(false);

            partidaDao.eliminarPorOrden(con, idOrdenCompra);
            boolean eliminado;
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM orden_compra WHERE id_orden_compra = ?")) {
                ps.setInt(1, idOrdenCompra);
                eliminado = ps.executeUpdate() > 0;
            }
            con.commit();
            return eliminado;
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "ELIMINAR_FISICO_ORDEN_COMPRA");
            throw DaoSupport.traducirYRegistrar(MODULO, "OrdenCompraDao", "ELIMINAR_FISICO_ORDEN_COMPRA",
                    "No se pudo eliminar la orden de compra definitivamente.", ex);
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    private OrdenCompra mapear(ResultSet rs) throws SQLException {
        Date f = rs.getDate("fecha_orden");
        int provRaw = rs.getInt("id_proveedor");
        Integer idProv = rs.wasNull() ? null : provRaw;
        return new OrdenCompra(
                rs.getInt("id_orden_compra"),
                rs.getInt("id_proyecto"),
                idProv,
                f != null ? f.toLocalDate() : null,
                rs.getBigDecimal("total_compra"),
                rs.getString("tiempo_entrega"),
                rs.getString("estado_orden"),
                rs.getBoolean("activo"),
                rs.getString("nombre_proyecto"),
                rs.getString("nombre_proveedor"),
                rs.getInt("total_partidas")
        );
    }
}
