package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.util.AppLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla cliente, incluyendo alta transaccional con telefono inicial.
 */
public class ClienteDao {

    private static final String MODULO = "Clientes";

    public Cliente crear(Cliente c) throws SoptecException {
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_CLIENTE")) {
            return crear(con, c);
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "CREAR_CLIENTE",
                    "No se pudo registrar el cliente en la base de datos.", ex);
        }
    }

    public Cliente crear(Connection con, Cliente c) throws SQLException, OperacionBDException {
        String sql = "INSERT INTO cliente (rfc, nombre_cliente, correo, calle, numero, colonia, estado, imagen, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getRfc());
            ps.setString(2, c.getNombreCliente());
            ps.setString(3, c.getCorreo());
            DaoSupport.setNullableString(ps, 4, c.getCalle());
            DaoSupport.setNullableString(ps, 5, c.getNumero());
            DaoSupport.setNullableString(ps, 6, c.getColonia());
            DaoSupport.setNullableString(ps, 7, c.getEstado());
            DaoSupport.setNullableString(ps, 8, c.getImagen());
            ps.setBoolean(9, c.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setIdCliente(keys.getInt(1));
                } else {
                    throw new OperacionBDException(MODULO, "CREAR_CLIENTE",
                            "El cliente se registró pero no se obtuvo su identificador generado.");
                }
            }
            return c;
        }
    }

    /**
     * Transaccion multi-tabla: inserta el cliente y su telefono inicial de forma atomica.
     * Si falla cualquier paso (o se provoca un error), ejecuta rollback completo.
     */
    public Cliente crearConTelefonoInicial(Cliente cliente, String telefonoInicial, boolean simularFallo)
            throws SoptecException {
        Connection con = null;
        try {
            con = Conexion.conectarStrict(MODULO, "CREAR_CLIENTE_CON_TELEFONO");
            con.setAutoCommit(false);

            crear(con, cliente);

            if (simularFallo) {
                throw new SQLException("Fallo simulado posterior al INSERT de cliente para comprobar rollback.", "45000", 9999);
            }

            if (telefonoInicial != null && !telefonoInicial.isBlank()) {
                String sqlTel = "INSERT INTO cliente_telefono (id_cliente, telefono, activo) VALUES (?, ?, 1)";
                try (PreparedStatement psTel = con.prepareStatement(sqlTel)) {
                    psTel.setInt(1, cliente.getIdCliente());
                    psTel.setString(2, telefonoInicial.trim());
                    psTel.executeUpdate();
                }
            }

            con.commit();
            AppLogger.registrarInfo(MODULO, "ClienteDao", "CREAR_CLIENTE_CON_TELEFONO",
                    "Transaccion confirmada para cliente ID=" + cliente.getIdCliente());
            return cliente;
        } catch (SQLException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_CLIENTE_CON_TELEFONO");
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "CREAR_CLIENTE_CON_TELEFONO",
                    "No se pudo completar el registro del cliente y su teléfono. Se revirtieron los cambios.", ex);
        } catch (SoptecException ex) {
            Conexion.revertirSilencioso(con, MODULO, "CREAR_CLIENTE_CON_TELEFONO");
            throw ex;
        } finally {
            Conexion.cerrarSilencioso(con);
        }
    }

    public Cliente obtenerPorId(int idCliente) throws SoptecException {
        String sql = "SELECT id_cliente, rfc, nombre_cliente, correo, calle, numero, colonia, estado, imagen, activo "
                + "FROM cliente WHERE id_cliente = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_POR_ID");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "CONSULTAR_POR_ID",
                    "Error al consultar el cliente solicitado.", ex);
        }
    }

    public List<Cliente> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT id_cliente, rfc, nombre_cliente, correo, calle, numero, colonia, estado, imagen, activo "
                + "FROM cliente "
                + (incluirInactivos ? "" : "WHERE activo = 1 ")
                + "ORDER BY nombre_cliente ASC, id_cliente DESC";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_CLIENTES");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "LISTAR_CLIENTES",
                    "Error al obtener el listado de clientes.", ex);
        }
    }

    public List<Cliente> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT id_cliente, rfc, nombre_cliente, correo, calle, numero, colonia, estado, imagen, activo "
                + "FROM cliente WHERE (nombre_cliente LIKE ? OR rfc LIKE ? OR correo LIKE ? OR colonia LIKE ?) "
                + (incluirInactivos ? "" : "AND activo = 1 ")
                + "ORDER BY nombre_cliente ASC";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_CLIENTES");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "BUSCAR_CLIENTES",
                    "Error al buscar clientes.", ex);
        }
    }

    public boolean actualizar(Cliente c) throws SoptecException {
        String sql = "UPDATE cliente SET rfc = ?, nombre_cliente = ?, correo = ?, calle = ?, numero = ?, "
                + "colonia = ?, estado = ?, imagen = ?, activo = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_CLIENTE");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getRfc());
            ps.setString(2, c.getNombreCliente());
            ps.setString(3, c.getCorreo());
            DaoSupport.setNullableString(ps, 4, c.getCalle());
            DaoSupport.setNullableString(ps, 5, c.getNumero());
            DaoSupport.setNullableString(ps, 6, c.getColonia());
            DaoSupport.setNullableString(ps, 7, c.getEstado());
            DaoSupport.setNullableString(ps, 8, c.getImagen());
            ps.setBoolean(9, c.isActivo());
            ps.setInt(10, c.getIdCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "ACTUALIZAR_CLIENTE",
                    "No se pudo actualizar la información del cliente.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idCliente, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_CLIENTE" : "ELIMINAR_LOGICO_CLIENTE";
        String sql = "UPDATE cliente SET activo = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", op,
                    "No se pudo cambiar el estado del cliente.", ex);
        }
    }

    public boolean eliminarFisico(int idCliente) throws SoptecException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_CLIENTE");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "ELIMINAR_FISICO_CLIENTE",
                    "No se pudo eliminar el cliente definitivamente.", ex);
        }
    }

    public boolean existeDuplicado(String rfc, String correo, Integer excluirId) throws SoptecException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE (UPPER(rfc) = UPPER(?) OR LOWER(correo) = LOWER(?)) "
                + (excluirId != null && excluirId > 0 ? "AND id_cliente <> ?" : "");
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_DUPLICADO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rfc);
            ps.setString(2, correo);
            if (excluirId != null && excluirId > 0) {
                ps.setInt(3, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "VERIFICAR_DUPLICADO",
                    "Error al validar duplicados de cliente.", ex);
        }
    }

    public boolean tieneRelacionesActivas(int idCliente, boolean soloActivos) throws SoptecException {
        String filtro = soloActivos ? " AND activo = 1" : "";
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM cliente_telefono WHERE id_cliente = ?" + filtro + ") + "
                + "(SELECT COUNT(*) FROM cita WHERE id_cliente = ?" + filtro + ") + "
                + "(SELECT COUNT(*) FROM proyecto WHERE id_cliente = ?" + filtro + ") AS total";
        try (Connection con = Conexion.conectarStrict(MODULO, "VERIFICAR_RELACIONES");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idCliente);
            ps.setInt(3, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteDao", "VERIFICAR_RELACIONES",
                    "Error al verificar registros relacionados del cliente.", ex);
        }
    }

    /**
     * Metodo heredado compatible.
     */
    public void guardarCliente(Cliente c) throws SoptecException {
        crear(c);
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("rfc"),
                rs.getString("nombre_cliente"),
                rs.getString("correo"),
                rs.getString("calle"),
                rs.getString("numero"),
                rs.getString("colonia"),
                rs.getString("estado"),
                rs.getString("imagen"),
                rs.getBoolean("activo")
        );
    }
}
