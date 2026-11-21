package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Pago;
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
 * DAO completo para la tabla pago (Proyecto 1:N Pago).
 */
public class PagoDao {

    private static final String MODULO = "Pagos";

    private static final String SELECT_BASE =
            "SELECT pg.id_pago, pg.id_proyecto, pg.concepto, pg.monto, pg.fecha_pago, "
            + "pg.estado_cuenta, pg.metodo_pago, pg.activo, "
            + "p.nombre_proyecto, c.nombre_cliente, c.rfc "
            + "FROM pago pg "
            + "INNER JOIN proyecto p ON p.id_proyecto = pg.id_proyecto "
            + "INNER JOIN cliente c ON c.id_cliente = p.id_cliente ";

    public Pago crear(Pago pago) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_PAGO")) {
            int id = crear(con, pago);
            return obtenerPorId(id);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "CREAR_PAGO",
                    "No se pudo registrar el pago en la base de datos.", ex);
        }
    }

    public int crear(Connection con, Pago pago) throws SQLException, OperacionBDException {
        String sql = "INSERT INTO pago (id_proyecto, concepto, monto, fecha_pago, estado_cuenta, metodo_pago, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getIdProyecto());
            DaoSupport.setNullableString(ps, 2, pago.getConcepto());
            ps.setBigDecimal(3, pago.getMonto());
            ps.setDate(4, Date.valueOf(pago.getFechaPago()));
            ps.setString(5, pago.getEstadoCuenta());
            DaoSupport.setNullableString(ps, 6, pago.getMetodoPago());
            ps.setBoolean(7, pago.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    pago.setIdPago(id);
                    return id;
                }
                throw new OperacionBDException(MODULO, "CREAR_PAGO",
                        "El pago se registró pero no se obtuvo su identificador.");
            }
        }
    }

    public Pago obtenerPorId(int idPago) throws SoptecException {
        String sql = SELECT_BASE + "WHERE pg.id_pago = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_PAGO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "CONSULTAR_PAGO",
                    "Error al consultar el pago solicitado.", ex);
        }
    }

    public List<Pago> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = SELECT_BASE
                + (incluirInactivos ? "" : "WHERE pg.activo = 1 ")
                + "ORDER BY pg.fecha_pago DESC, pg.id_pago DESC";
        List<Pago> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PAGOS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "LISTAR_PAGOS",
                    "Error al obtener el listado de pagos.", ex);
        }
    }

    public List<Pago> listarPorProyecto(int idProyecto, boolean incluirInactivos) throws SoptecException {
        String sql = SELECT_BASE
                + "WHERE pg.id_proyecto = ? "
                + (incluirInactivos ? "" : "AND pg.activo = 1 ")
                + "ORDER BY pg.fecha_pago DESC, pg.id_pago DESC";
        List<Pago> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_PAGOS_PROYECTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "LISTAR_PAGOS_PROYECTO",
                    "Error al consultar los pagos del proyecto.", ex);
        }
    }

    public List<Pago> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = SELECT_BASE
                + "WHERE (pg.concepto LIKE ? OR pg.estado_cuenta LIKE ? OR IFNULL(pg.metodo_pago, '') LIKE ? "
                + "OR p.nombre_proyecto LIKE ? OR c.nombre_cliente LIKE ? OR c.rfc LIKE ?) "
                + (incluirInactivos ? "" : "AND pg.activo = 1 ")
                + "ORDER BY pg.fecha_pago DESC, pg.id_pago DESC";
        List<Pago> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_PAGOS");
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "BUSCAR_PAGOS",
                    "Error al buscar pagos.", ex);
        }
    }

    public boolean actualizar(Pago pago) throws SoptecException {
        String sql = "UPDATE pago SET id_proyecto = ?, concepto = ?, monto = ?, fecha_pago = ?, "
                + "estado_cuenta = ?, metodo_pago = ?, activo = ? WHERE id_pago = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_PAGO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pago.getIdProyecto());
            DaoSupport.setNullableString(ps, 2, pago.getConcepto());
            ps.setBigDecimal(3, pago.getMonto());
            ps.setDate(4, Date.valueOf(pago.getFechaPago()));
            ps.setString(5, pago.getEstadoCuenta());
            DaoSupport.setNullableString(ps, 6, pago.getMetodoPago());
            ps.setBoolean(7, pago.isActivo());
            ps.setInt(8, pago.getIdPago());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "ACTUALIZAR_PAGO",
                    "No se pudo actualizar el pago.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idPago, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_PAGO" : "ELIMINAR_LOGICO_PAGO";
        String sql = "UPDATE pago SET activo = ? WHERE id_pago = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idPago);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", op,
                    "No se pudo cambiar el estado del pago.", ex);
        }
    }

    public boolean eliminarFisico(int idPago) throws SoptecException {
        String sql = "DELETE FROM pago WHERE id_pago = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_PAGO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "ELIMINAR_FISICO_PAGO",
                    "No se pudo eliminar el pago definitivamente.", ex);
        }
    }

    public BigDecimal sumarPagosActivosPorProyecto(int idProyecto) throws SoptecException {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE id_proyecto = ? AND activo = 1";
        try (Connection con = Conexion.conectarStrict(MODULO, "SUMAR_PAGOS_PROYECTO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal suma = rs.getBigDecimal(1);
                    return suma != null ? suma : BigDecimal.ZERO;
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "PagoDao", "SUMAR_PAGOS_PROYECTO",
                    "Error al calcular los pagos acumulados del proyecto.", ex);
        }
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        Date f = rs.getDate("fecha_pago");
        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_proyecto"),
                rs.getString("concepto"),
                rs.getBigDecimal("monto"),
                f != null ? f.toLocalDate() : null,
                rs.getString("estado_cuenta"),
                rs.getString("metodo_pago"),
                rs.getBoolean("activo"),
                rs.getString("nombre_proyecto"),
                rs.getString("nombre_cliente"),
                rs.getString("rfc")
        );
    }
}
