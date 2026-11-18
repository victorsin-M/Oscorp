package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.ClienteDao;
import com.mycompany.soptec.it.dao.ClienteTelefonoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio para telefonos asociados a clientes.
 */
public class ClienteTelefonoService {

    private static final String MODULO = "TelefonosCliente";
    private final ClienteTelefonoDao dao = new ClienteTelefonoDao();
    private final ClienteDao clienteDao = new ClienteDao();

    public ClienteTelefono crear(ClienteTelefono ct) throws SoptecException {
        try {
            validar(ct, "CREAR_TELEFONO");
            if (dao.obtenerPorClave(ct.getIdCliente(), ct.getTelefono()) != null) {
                throw new ValidacionException(MODULO, "CREAR_TELEFONO",
                        "Este teléfono ya está registrado para el cliente seleccionado.");
            }
            ct.setActivo(true);
            ClienteTelefono creado = dao.crear(ct);
            AppLogger.registrarInfo(MODULO, "ClienteTelefonoService", "CREAR_TELEFONO",
                    "Telefono agregado para cliente ID=" + ct.getIdCliente());
            return creado;
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", "CREAR_TELEFONO", ex);
            throw ex;
        }
    }

    public ClienteTelefono consultarPorClave(int idCliente, String telefono) throws SoptecException {
        try {
            if (idCliente <= 0) {
                throw new ValidacionException(MODULO, "CONSULTAR_TELEFONO", "Selecciona un cliente válido.");
            }
            String tel = Validador.validarTelefono(telefono, MODULO, "CONSULTAR_TELEFONO");
            return dao.obtenerPorClave(idCliente, tel);
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", "CONSULTAR_TELEFONO", ex);
            throw ex;
        }
    }

    public List<ClienteTelefono> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<ClienteTelefono> listarPorCliente(int idCliente, boolean incluirInactivos) throws SoptecException {
        return dao.listarPorCliente(idCliente, incluirInactivos);
    }

    public List<ClienteTelefono> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(int idClienteOriginal, String telefonoOriginal, ClienteTelefono nuevo)
            throws SoptecException {
        try {
            if (idClienteOriginal <= 0 || telefonoOriginal == null || telefonoOriginal.isBlank()) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_TELEFONO",
                        "Selecciona primero el teléfono de la tabla que deseas actualizar.");
            }
            validar(nuevo, "ACTUALIZAR_TELEFONO");
            boolean cambioClave = idClienteOriginal != nuevo.getIdCliente()
                    || !telefonoOriginal.equals(nuevo.getTelefono());
            if (cambioClave && dao.obtenerPorClave(nuevo.getIdCliente(), nuevo.getTelefono()) != null) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_TELEFONO",
                        "Ya existe ese número registrado para el cliente seleccionado.");
            }
            if (!dao.actualizar(idClienteOriginal, telefonoOriginal, nuevo)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_TELEFONO",
                        "No se encontró el teléfono original para actualizar.");
            }
            AppLogger.registrarInfo(MODULO, "ClienteTelefonoService", "ACTUALIZAR_TELEFONO",
                    "Telefono actualizado para cliente ID=" + nuevo.getIdCliente());
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", "ACTUALIZAR_TELEFONO", ex);
            throw ex;
        }
    }

    public void desactivar(int idCliente, String telefono) throws SoptecException {
        cambiarEstado(idCliente, telefono, false, "DESACTIVAR_TELEFONO");
    }

    public void reactivar(int idCliente, String telefono) throws SoptecException {
        cambiarEstado(idCliente, telefono, true, "REACTIVAR_TELEFONO");
    }

    private void cambiarEstado(int idCliente, String telefono, boolean activo, String op) throws SoptecException {
        if (idCliente <= 0 || telefono == null || telefono.isBlank()) {
            ValidacionException ex = new ValidacionException(MODULO, op, "Selecciona un teléfono.");
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", op, ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idCliente, telefono, activo);
        AppLogger.registrarInfo(MODULO, "ClienteTelefonoService", op,
                "Estado de telefono actualizado para cliente ID=" + idCliente);
    }

    public void eliminarFisico(int idCliente, String telefono) throws SoptecException {
        if (idCliente <= 0 || telefono == null || telefono.isBlank()) {
            ValidacionException ex = new ValidacionException(MODULO, "ELIMINAR_FISICO_TELEFONO",
                    "Selecciona el teléfono que deseas eliminar.");
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", "ELIMINAR_FISICO_TELEFONO", ex);
            throw ex;
        }
        if (!dao.eliminarFisico(idCliente, telefono)) {
            ValidacionException ex = new ValidacionException(MODULO, "ELIMINAR_FISICO_TELEFONO",
                    "El teléfono seleccionado ya no existe.");
            AppLogger.registrarExcepcion(MODULO, "ClienteTelefonoService", "ELIMINAR_FISICO_TELEFONO", ex);
            throw ex;
        }
        AppLogger.registrarInfo(MODULO, "ClienteTelefonoService", "ELIMINAR_FISICO_TELEFONO",
                "Telefono eliminado fisicamente para cliente ID=" + idCliente);
    }

    private void validar(ClienteTelefono ct, String operacion) throws SoptecException {
        if (ct == null || ct.getIdCliente() <= 0) {
            throw new ValidacionException(MODULO, operacion, "Debes seleccionar un cliente válido de la lista.");
        }
        Cliente padre = clienteDao.obtenerPorId(ct.getIdCliente());
        if (padre == null) {
            throw new IntegridadReferencialException(MODULO, operacion,
                    "El cliente seleccionado no existe en la base de datos.");
        }
        if (!padre.isActivo()) {
            throw new ValidacionException(MODULO, operacion,
                    "El cliente '" + padre.getNombreCliente() + "' está inactivo y no puede recibir teléfonos nuevos.");
        }
        ct.setTelefono(Validador.validarTelefono(ct.getTelefono(), MODULO, operacion));
    }
}
