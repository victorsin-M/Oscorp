package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.ClienteDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio y validaciones para la gestion de clientes.
 */
public class ClienteService {

    private static final String MODULO = "Clientes";
    private final ClienteDao dao = new ClienteDao();

    public Cliente crear(Cliente c) throws SoptecException {
        try {
            validarYNormalizar(c, "CREAR_CLIENTE");
            if (dao.existeDuplicado(c.getRfc(), c.getCorreo(), null)) {
                throw new ValidacionException(MODULO, "CREAR_CLIENTE",
                        "Ya existe un cliente registrado con el mismo RFC o correo electrónico.");
            }
            c.setActivo(true);
            Cliente creado = dao.crear(c);
            AppLogger.registrarInfo(MODULO, "ClienteService", "CREAR_CLIENTE",
                    "Cliente registrado ID=" + creado.getIdCliente());
            return creado;
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "CREAR_CLIENTE", ex);
            throw ex;
        }
    }

    public Cliente crearConTelefonoInicial(Cliente c, String telefonoInicial, boolean simularFallo)
            throws SoptecException {
        try {
            validarYNormalizar(c, "CREAR_CLIENTE_CON_TELEFONO");
            if (telefonoInicial != null && !telefonoInicial.isBlank()) {
                telefonoInicial = Validador.validarTelefono(telefonoInicial, MODULO, "CREAR_CLIENTE_CON_TELEFONO");
            }
            if (dao.existeDuplicado(c.getRfc(), c.getCorreo(), null)) {
                throw new ValidacionException(MODULO, "CREAR_CLIENTE_CON_TELEFONO",
                        "Ya existe un cliente registrado con el mismo RFC o correo electrónico.");
            }
            c.setActivo(true);
            return dao.crearConTelefonoInicial(c, telefonoInicial, simularFallo);
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "CREAR_CLIENTE_CON_TELEFONO", ex);
            throw ex;
        }
    }

    public Cliente consultarPorId(int idCliente) throws SoptecException {
        if (idCliente <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID del cliente debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idCliente);
    }

    public List<Cliente> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Cliente> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Cliente c) throws SoptecException {
        try {
            if (c.getIdCliente() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CLIENTE",
                        "Selecciona primero el cliente que deseas actualizar.");
            }
            validarYNormalizar(c, "ACTUALIZAR_CLIENTE");
            if (dao.existeDuplicado(c.getRfc(), c.getCorreo(), c.getIdCliente())) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CLIENTE",
                        "Ya existe otro cliente con ese RFC o correo electrónico.");
            }
            if (!dao.actualizar(c)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CLIENTE",
                        "El cliente seleccionado ya no existe en la base de datos.");
            }
            AppLogger.registrarInfo(MODULO, "ClienteService", "ACTUALIZAR_CLIENTE",
                    "Cliente actualizado ID=" + c.getIdCliente());
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "ACTUALIZAR_CLIENTE", ex);
            throw ex;
        }
    }

    public void desactivar(int idCliente) throws SoptecException {
        try {
            if (idCliente <= 0) {
                throw new ValidacionException(MODULO, "DESACTIVAR_CLIENTE", "Selecciona un cliente.");
            }
            if (dao.tieneRelacionesActivas(idCliente, true)) {
                throw new IntegridadReferencialException(MODULO, "DESACTIVAR_CLIENTE",
                        "No se puede desactivar el cliente porque tiene teléfonos, citas o proyectos activos relacionados.");
            }
            if (!dao.cambiarEstadoActivo(idCliente, false)) {
                throw new ValidacionException(MODULO, "DESACTIVAR_CLIENTE", "No se encontró el cliente.");
            }
            AppLogger.registrarInfo(MODULO, "ClienteService", "DESACTIVAR_CLIENTE",
                    "Cliente desactivado ID=" + idCliente);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "DESACTIVAR_CLIENTE", ex);
            throw ex;
        }
    }

    public void reactivar(int idCliente) throws SoptecException {
        if (idCliente <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_CLIENTE", "Selecciona un cliente.");
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "REACTIVAR_CLIENTE", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idCliente, true);
        AppLogger.registrarInfo(MODULO, "ClienteService", "REACTIVAR_CLIENTE",
                "Cliente reactivado ID=" + idCliente);
    }

    public void eliminarFisico(int idCliente) throws SoptecException {
        try {
            if (idCliente <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_CLIENTE", "Selecciona un cliente.");
            }
            if (dao.tieneRelacionesActivas(idCliente, false)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_CLIENTE",
                        "No se puede eliminar el cliente porque tiene teléfonos, citas o proyectos asociados en la base de datos.");
            }
            if (!dao.eliminarFisico(idCliente)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_CLIENTE", "No se encontró el cliente.");
            }
            AppLogger.registrarInfo(MODULO, "ClienteService", "ELIMINAR_FISICO_CLIENTE",
                    "Cliente eliminado fisicamente ID=" + idCliente);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ClienteService", "ELIMINAR_FISICO_CLIENTE", ex);
            throw ex;
        }
    }

    private void validarYNormalizar(Cliente c, String operacion) throws ValidacionException {
        if (c == null) {
            throw new ValidacionException(MODULO, operacion, "Los datos del cliente son requeridos.");
        }
        c.setRfc(Validador.validarRfc(c.getRfc(), MODULO, operacion));
        c.setNombreCliente(Validador.requerido(c.getNombreCliente(), "Nombre del cliente", MODULO, operacion));
        Validador.longitudMaxima(c.getNombreCliente(), 100, "Nombre del cliente", MODULO, operacion);
        c.setCorreo(Validador.validarCorreo(c.getCorreo(), MODULO, operacion));

        c.setCalle(Validador.opcional(c.getCalle()));
        c.setNumero(Validador.opcional(c.getNumero()));
        c.setColonia(Validador.opcional(c.getColonia()));
        c.setEstado(Validador.opcional(c.getEstado()));
        c.setImagen(Validador.opcional(c.getImagen()));

        Validador.longitudMaxima(c.getCalle(), 100, "Calle", MODULO, operacion);
        Validador.longitudMaxima(c.getNumero(), 20, "Número", MODULO, operacion);
        Validador.longitudMaxima(c.getColonia(), 100, "Colonia", MODULO, operacion);
        Validador.longitudMaxima(c.getEstado(), 50, "Estado", MODULO, operacion);
        Validador.longitudMaxima(c.getImagen(), 500, "Ruta de imagen", MODULO, operacion);
    }
}
