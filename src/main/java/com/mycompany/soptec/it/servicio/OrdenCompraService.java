package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.MaterialDao;
import com.mycompany.soptec.it.dao.OrdenCompraDao;
import com.mycompany.soptec.it.dao.ProveedorDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.OrdenCompra;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio transaccionales para orden_compra y sus partidas M:N orden_compra_material.
 */
public class OrdenCompraService {

    private static final String MODULO = "OrdenesCompra";
    private final OrdenCompraDao dao = new OrdenCompraDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final ProveedorDao proveedorDao = new ProveedorDao();
    private final MaterialDao materialDao = new MaterialDao();

    public OrdenCompra crearConPartidas(OrdenCompra orden, List<OrdenCompraMaterial> partidas, boolean simularFallo)
            throws SoptecException {
        try {
            validarOrdenYPartidas(orden, partidas, "CREAR_ORDEN_COMPRA");
            orden.setActivo(true);
            if (orden.getEstadoOrden() == null || orden.getEstadoOrden().isBlank()) {
                orden.setEstadoOrden("Solicitada");
            }
            return dao.crearConPartidas(orden, partidas, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "OrdenCompraService", "CREAR_ORDEN_COMPRA", ex);
            throw ex;
        }
    }

    public OrdenCompra actualizarConPartidas(OrdenCompra orden, List<OrdenCompraMaterial> partidas)
            throws SoptecException {
        try {
            if (orden == null || orden.getIdOrdenCompra() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_ORDEN_COMPRA",
                        "Selecciona primero una orden de compra para actualizar.");
            }
            validarOrdenYPartidas(orden, partidas, "ACTUALIZAR_ORDEN_COMPRA");
            return dao.actualizarConPartidas(orden, partidas);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "OrdenCompraService", "ACTUALIZAR_ORDEN_COMPRA", ex);
            throw ex;
        }
    }

    public OrdenCompra recibirOrden(int idOrdenCompra, boolean simularFallo) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "RECIBIR_ORDEN_COMPRA", "Selecciona una orden de compra.");
        }
        return dao.recibirOrden(idOrdenCompra, simularFallo);
    }

    public void cancelarOrden(int idOrdenCompra) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "CANCELAR_ORDEN_COMPRA", "Selecciona una orden de compra.");
        }
        OrdenCompra actual = dao.obtenerPorId(idOrdenCompra);
        if (actual == null) {
            throw new ValidacionException(MODULO, "CANCELAR_ORDEN_COMPRA", "La orden de compra no existe.");
        }
        if ("Recibida".equalsIgnoreCase(actual.getEstadoOrden())) {
            throw new ValidacionException(MODULO, "CANCELAR_ORDEN_COMPRA",
                    "No se puede cancelar una orden de compra que ya fue recibida en almacén.");
        }
        dao.actualizarEstado(idOrdenCompra, "Cancelada");
        AppLogger.registrarInfo(MODULO, "OrdenCompraService", "CANCELAR_ORDEN_COMPRA",
                "Orden cancelada ID=" + idOrdenCompra);
    }

    public OrdenCompra consultarPorId(int idOrdenCompra) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "CONSULTAR_ORDEN_COMPRA",
                    "El ID de la orden de compra debe ser mayor a cero.");
        }
        return dao.obtenerPorId(idOrdenCompra);
    }

    public List<OrdenCompra> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<OrdenCompra> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void desactivar(int idOrdenCompra) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "DESACTIVAR_ORDEN_COMPRA", "Selecciona una orden de compra.");
        }
        if (!dao.cambiarEstadoActivo(idOrdenCompra, false)) {
            throw new ValidacionException(MODULO, "DESACTIVAR_ORDEN_COMPRA", "La orden de compra no existe.");
        }
        AppLogger.registrarInfo(MODULO, "OrdenCompraService", "DESACTIVAR_ORDEN_COMPRA",
                "Orden desactivada ID=" + idOrdenCompra);
    }

    public void reactivar(int idOrdenCompra) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "REACTIVAR_ORDEN_COMPRA", "Selecciona una orden de compra.");
        }
        if (!dao.cambiarEstadoActivo(idOrdenCompra, true)) {
            throw new ValidacionException(MODULO, "REACTIVAR_ORDEN_COMPRA", "La orden de compra no existe.");
        }
        AppLogger.registrarInfo(MODULO, "OrdenCompraService", "REACTIVAR_ORDEN_COMPRA",
                "Orden reactivada ID=" + idOrdenCompra);
    }

    public void eliminarFisico(int idOrdenCompra) throws SoptecException {
        if (idOrdenCompra <= 0) {
            throw new ValidacionException(MODULO, "ELIMINAR_FISICO_ORDEN_COMPRA", "Selecciona una orden de compra.");
        }
        if (!dao.eliminarFisico(idOrdenCompra)) {
            throw new ValidacionException(MODULO, "ELIMINAR_FISICO_ORDEN_COMPRA", "La orden de compra no existe.");
        }
        AppLogger.registrarInfo(MODULO, "OrdenCompraService", "ELIMINAR_FISICO_ORDEN_COMPRA",
                "Orden eliminada fisicamente ID=" + idOrdenCompra);
    }

    private void validarOrdenYPartidas(OrdenCompra orden, List<OrdenCompraMaterial> partidas, String op)
            throws SoptecException {
        if (orden == null) {
            throw new ValidacionException(MODULO, op, "Los datos de la orden de compra son obligatorios.");
        }
        if (orden.getIdProyecto() <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proyecto para la orden de compra.");
        }
        Proyecto proy = proyectoDao.obtenerPorId(orden.getIdProyecto());
        if (proy == null) {
            throw new IntegridadReferencialException(MODULO, op, "El proyecto seleccionado no existe.");
        }
        if (!proy.isActivo()) {
            throw new ValidacionException(MODULO, op, "El proyecto seleccionado está inactivo.");
        }
        if (orden.getIdProveedor() != null && orden.getIdProveedor() > 0) {
            Proveedor prov = proveedorDao.obtenerPorId(orden.getIdProveedor());
            if (prov == null) {
                throw new IntegridadReferencialException(MODULO, op, "El proveedor seleccionado no existe.");
            }
            if (!prov.isActivo()) {
                throw new ValidacionException(MODULO, op, "El proveedor seleccionado está inactivo.");
            }
        }
        if (orden.getFechaOrden() == null) {
            throw new ValidacionException(MODULO, op, "La fecha de la orden es obligatoria.");
        }
        orden.setTiempoEntrega(Validador.opcional(orden.getTiempoEntrega()));
        Validador.longitudMaxima(orden.getTiempoEntrega(), 50, "Tiempo de entrega", MODULO, op);
        if (orden.getEstadoOrden() == null || orden.getEstadoOrden().isBlank()) {
            orden.setEstadoOrden("Solicitada");
        }
        Validador.longitudMaxima(orden.getEstadoOrden(), 30, "Estado de la orden", MODULO, op);

        if (partidas == null || partidas.isEmpty()) {
            throw new ValidacionException(MODULO, op,
                    "Debes agregar al menos una partida de material a la orden de compra.");
        }
        for (OrdenCompraMaterial item : partidas) {
            if (item == null || item.getIdMaterial() <= 0) {
                throw new ValidacionException(MODULO, op, "Todas las partidas deben tener un material válido.");
            }
            if (item.getCantidad() == null || item.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidacionException(MODULO, op, "La cantidad de cada material debe ser mayor a cero.");
            }
            Material mat = materialDao.obtenerPorId(item.getIdMaterial());
            if (mat == null) {
                throw new IntegridadReferencialException(MODULO, op,
                        "Uno de los materiales de la orden ya no existe en la base de datos.");
            }
            if (!mat.isActivo()) {
                throw new ValidacionException(MODULO, op,
                        "El material '" + mat.getDescripcionMaterial() + "' está inactivo.");
            }
        }
    }
}
