package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.PagoDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio para la gestion CRUD de pagos asociados a proyectos.
 */
public class PagoService {

    private static final String MODULO = "Pagos";
    private final PagoDao dao = new PagoDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();

    public Pago crear(Pago pago) throws SoptecException {
        try {
            validar(pago, "CREAR_PAGO");
            pago.setActivo(true);
            Pago creado = dao.crear(pago);
            AppLogger.registrarInfo(MODULO, "PagoService", "CREAR_PAGO",
                    "Pago registrado ID=" + creado.getIdPago() + " proyecto=" + creado.getIdProyecto());
            return creado;
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "PagoService", "CREAR_PAGO", ex);
            throw ex;
        }
    }

    public Pago consultarPorId(int idPago) throws SoptecException {
        if (idPago <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_PAGO",
                    "El ID del pago debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "PagoService", "CONSULTAR_PAGO", ex);
            throw ex;
        }
        return dao.obtenerPorId(idPago);
    }

    public List<Pago> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Pago> listarPorProyecto(int idProyecto, boolean incluirInactivos) throws SoptecException {
        if (idProyecto <= 0) {
            throw new ValidacionException(MODULO, "LISTAR_PAGOS_PROYECTO",
                    "Selecciona un proyecto válido.");
        }
        return dao.listarPorProyecto(idProyecto, incluirInactivos);
    }

    public List<Pago> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Pago pago) throws SoptecException {
        try {
            if (pago == null || pago.getIdPago() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PAGO",
                        "Selecciona primero el pago que deseas actualizar.");
            }
            validar(pago, "ACTUALIZAR_PAGO");
            if (!dao.actualizar(pago)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PAGO", "El pago ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "PagoService", "ACTUALIZAR_PAGO",
                    "Pago actualizado ID=" + pago.getIdPago());
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "PagoService", "ACTUALIZAR_PAGO", ex);
            throw ex;
        }
    }

    public void desactivar(int idPago) throws SoptecException {
        if (idPago <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_PAGO", "Selecciona un pago.");
            AppLogger.registrarExcepcion(MODULO, "PagoService", "DESACTIVAR_PAGO", ex);
            throw ex;
        }
        if (!dao.cambiarEstadoActivo(idPago, false)) {
            throw new ValidacionException(MODULO, "DESACTIVAR_PAGO", "No se encontró el pago seleccionado.");
        }
        AppLogger.registrarInfo(MODULO, "PagoService", "DESACTIVAR_PAGO", "Pago desactivado ID=" + idPago);
    }

    public void reactivar(int idPago) throws SoptecException {
        if (idPago <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_PAGO", "Selecciona un pago.");
            AppLogger.registrarExcepcion(MODULO, "PagoService", "REACTIVAR_PAGO", ex);
            throw ex;
        }
        if (!dao.cambiarEstadoActivo(idPago, true)) {
            throw new ValidacionException(MODULO, "REACTIVAR_PAGO", "No se encontró el pago seleccionado.");
        }
        AppLogger.registrarInfo(MODULO, "PagoService", "REACTIVAR_PAGO", "Pago reactivado ID=" + idPago);
    }

    public void eliminarFisico(int idPago) throws SoptecException {
        if (idPago <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "ELIMINAR_FISICO_PAGO", "Selecciona un pago.");
            AppLogger.registrarExcepcion(MODULO, "PagoService", "ELIMINAR_FISICO_PAGO", ex);
            throw ex;
        }
        if (!dao.eliminarFisico(idPago)) {
            throw new ValidacionException(MODULO, "ELIMINAR_FISICO_PAGO", "No se encontró el pago seleccionado.");
        }
        AppLogger.registrarInfo(MODULO, "PagoService", "ELIMINAR_FISICO_PAGO", "Pago eliminado fisicamente ID=" + idPago);
    }

    public BigDecimal sumarPagosActivosPorProyecto(int idProyecto) throws SoptecException {
        if (idProyecto <= 0) {
            return BigDecimal.ZERO;
        }
        return dao.sumarPagosActivosPorProyecto(idProyecto);
    }

    private void validar(Pago pago, String op) throws SoptecException {
        if (pago == null) {
            throw new ValidacionException(MODULO, op, "Los datos del pago son obligatorios.");
        }
        if (pago.getIdProyecto() <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proyecto de la lista.");
        }
        Proyecto proy = proyectoDao.obtenerPorId(pago.getIdProyecto());
        if (proy == null) {
            throw new IntegridadReferencialException(MODULO, op,
                    "El proyecto seleccionado no existe en la base de datos.");
        }
        if (!proy.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El proyecto '" + proy.getNombreProyecto() + "' está inactivo y no admite nuevos pagos.");
        }
        pago.setConcepto(Validador.requerido(pago.getConcepto(), "Concepto", MODULO, op));
        Validador.longitudMaxima(pago.getConcepto(), 150, "Concepto", MODULO, op);
        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException(MODULO, op, "El monto del pago debe ser mayor a cero.");
        }
        if (pago.getFechaPago() == null) {
            throw new ValidacionException(MODULO, op, "La fecha de pago es obligatoria.");
        }
        pago.setEstadoCuenta(Validador.requerido(pago.getEstadoCuenta(), "Estado de cuenta", MODULO, op));
        Validador.longitudMaxima(pago.getEstadoCuenta(), 30, "Estado de cuenta", MODULO, op);
        pago.setMetodoPago(Validador.opcional(pago.getMetodoPago()));
        Validador.longitudMaxima(pago.getMetodoPago(), 40, "Método de pago", MODULO, op);
    }
}
