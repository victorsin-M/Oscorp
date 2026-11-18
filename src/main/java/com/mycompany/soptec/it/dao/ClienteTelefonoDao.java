package com.mycompany.soptec.it.dao;

import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO completo para la tabla cliente_telefono (clave compuesta id_cliente + telefono).
 */
public class ClienteTelefonoDao {

    private static final String MODULO = "TelefonosCliente";

    public ClienteTelefono crear(ClienteTelefono ct) throws SoptecException {
        String sql = "INSERT INTO cliente_telefono (id_cliente, telefono, activo) VALUES (?, ?, ?)";
        try (Connection con = Conexion.conectarStrict(MODULO, "CREAR_TELEFONO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ct.getIdCliente());
            ps.setString(2, ct.getTelefono());
            ps.setBoolean(3, ct.isActivo());
            ps.executeUpdate();
            return obtenerPorClave(ct.getIdCliente(), ct.getTelefono());
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "CREAR_TELEFONO",
                    "No se pudo registrar el teléfono para el cliente seleccionado.", ex);
        }
    }

    public ClienteTelefono obtenerPorClave(int idCliente, String telefono) throws SoptecException {
        String sql = "SELECT ct.id_cliente, ct.telefono, ct.activo, c.nombre_cliente, c.rfc "
                + "FROM cliente_telefono ct "
                + "INNER JOIN cliente c ON c.id_cliente = ct.id_cliente "
                + "WHERE ct.id_cliente = ? AND ct.telefono = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "CONSULTAR_TELEFONO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "CONSULTAR_TELEFONO",
                    "Error al consultar el teléfono del cliente.", ex);
        }
    }

    public List<ClienteTelefono> listarPorCliente(int idCliente, boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT ct.id_cliente, ct.telefono, ct.activo, c.nombre_cliente, c.rfc "
                + "FROM cliente_telefono ct "
                + "INNER JOIN cliente c ON c.id_cliente = ct.id_cliente "
                + "WHERE ct.id_cliente = ? "
                + (incluirInactivos ? "" : "AND ct.activo = 1 ")
                + "ORDER BY ct.telefono ASC";
        List<ClienteTelefono> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_POR_CLIENTE");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "LISTAR_POR_CLIENTE",
                    "Error al listar los teléfonos del cliente.", ex);
        }
    }

    public List<ClienteTelefono> listarTodos(boolean incluirInactivos) throws SoptecException {
        String sql = "SELECT ct.id_cliente, ct.telefono, ct.activo, c.nombre_cliente, c.rfc "
                + "FROM cliente_telefono ct "
                + "INNER JOIN cliente c ON c.id_cliente = ct.id_cliente "
                + (incluirInactivos ? "" : "WHERE ct.activo = 1 AND c.activo = 1 ")
                + "ORDER BY c.nombre_cliente ASC, ct.telefono ASC";
        List<ClienteTelefono> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "LISTAR_TELEFONOS");
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "LISTAR_TELEFONOS",
                    "Error al obtener el listado de teléfonos.", ex);
        }
    }

    public List<ClienteTelefono> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        String like = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        String sql = "SELECT ct.id_cliente, ct.telefono, ct.activo, c.nombre_cliente, c.rfc "
                + "FROM cliente_telefono ct "
                + "INNER JOIN cliente c ON c.id_cliente = ct.id_cliente "
                + "WHERE (ct.telefono LIKE ? OR c.nombre_cliente LIKE ? OR c.rfc LIKE ?) "
                + (incluirInactivos ? "" : "AND ct.activo = 1 AND c.activo = 1 ")
                + "ORDER BY c.nombre_cliente ASC, ct.telefono ASC";
        List<ClienteTelefono> lista = new ArrayList<>();
        try (Connection con = Conexion.conectarStrict(MODULO, "BUSCAR_TELEFONOS");
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
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "BUSCAR_TELEFONOS",
                    "Error al buscar teléfonos.", ex);
        }
    }

    public boolean actualizar(int idClienteOriginal, String telefonoOriginal, ClienteTelefono nuevo)
            throws SoptecException {
        String sql = "UPDATE cliente_telefono SET id_cliente = ?, telefono = ?, activo = ? "
                + "WHERE id_cliente = ? AND telefono = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ACTUALIZAR_TELEFONO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevo.getIdCliente());
            ps.setString(2, nuevo.getTelefono());
            ps.setBoolean(3, nuevo.isActivo());
            ps.setInt(4, idClienteOriginal);
            ps.setString(5, telefonoOriginal);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "ACTUALIZAR_TELEFONO",
                    "No se pudo actualizar el teléfono del cliente.", ex);
        }
    }

    public boolean cambiarEstadoActivo(int idCliente, String telefono, boolean activo) throws SoptecException {
        String op = activo ? "REACTIVAR_TELEFONO" : "ELIMINAR_LOGICO_TELEFONO";
        String sql = "UPDATE cliente_telefono SET activo = ? WHERE id_cliente = ? AND telefono = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, op);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idCliente);
            ps.setString(3, telefono);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", op,
                    "No se pudo cambiar el estado del teléfono.", ex);
        }
    }

    public boolean eliminarFisico(int idCliente, String telefono) throws SoptecException {
        String sql = "DELETE FROM cliente_telefono WHERE id_cliente = ? AND telefono = ?";
        try (Connection con = Conexion.conectarStrict(MODULO, "ELIMINAR_FISICO_TELEFONO");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, telefono);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DaoSupport.traducirYRegistrar(MODULO, "ClienteTelefonoDao", "ELIMINAR_FISICO_TELEFONO",
                    "No se pudo eliminar el teléfono.", ex);
        }
    }

    private ClienteTelefono mapear(ResultSet rs) throws SQLException {
        return new ClienteTelefono(
                rs.getInt("id_cliente"),
                rs.getString("telefono"),
                rs.getBoolean("activo"),
                rs.getString("nombre_cliente"),
                rs.getString("rfc")
        );
    }
}
