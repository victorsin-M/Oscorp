package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.ClienteDao;
import com.mycompany.soptec.it.dao.EmpleadoDao;
import com.mycompany.soptec.it.dao.MaterialDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio para proyectos y su relacion con clientes, levantamientos, materiales, tecnicos y pagos.
 */
public class ProyectoService {

    private static final String MODULO = "Proyectos";
    private final ProyectoDao dao = new ProyectoDao();
    private final ClienteDao clienteDao = new ClienteDao();
    private final MaterialDao materialDao = new MaterialDao();
    private final EmpleadoDao empleadoDao = new EmpleadoDao();

    public Proyecto crear(Proyecto p) throws SoptecException {
        try {
            validar(p, "CREAR_PROYECTO");
            p.setActivo(true);
            Proyecto creado = dao.crear(p);
            AppLogger.registrarInfo(MODULO, "ProyectoService", "CREAR_PROYECTO",
                    "Proyecto creado ID=" + creado.getIdProyecto());
            return creado;
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "CREAR_PROYECTO", ex);
            throw ex;
        }
    }

    public Proyecto crearConLevantamientoInicial(Proyecto p, Levantamiento l, boolean simularFallo)
            throws SoptecException {
        try {
            validar(p, "CREAR_PROYECTO_CON_LEVANTAMIENTO");
            if (l != null && l.getFechaVisita() == null) {
                throw new ValidacionException(MODULO, "CREAR_PROYECTO_CON_LEVANTAMIENTO",
                        "La fecha de visita del levantamiento inicial es obligatoria.");
            }
            p.setActivo(true);
            return dao.crearConLevantamientoInicial(p, l, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "CREAR_PROYECTO_CON_LEVANTAMIENTO", ex);
            throw ex;
        }
    }

    /**
     * Registra un proyecto (venta) junto con sus materiales M:N, empleados M:N y pago inicial
     * dentro de una sola transaccion atomica con rollback garantizado.
     */
    public Proyecto crearProyectoIntegral(Proyecto p, List<ProyectoMaterial> materiales,
            List<Integer> idsEmpleados, Pago pagoInicial, boolean simularFallo) throws SoptecException {
        String op = "CREAR_PROYECTO_INTEGRAL";
        try {
            validar(p, op);
            p.setActivo(true);
            if (materiales != null) {
                for (ProyectoMaterial pm : materiales) {
                    if (pm == null || pm.getIdMaterial() <= 0) {
                        throw new ValidacionException(MODULO, op, "Todos los materiales asignados deben ser válidos.");
                    }
                    if (pm.getCantidad() == null || pm.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ValidacionException(MODULO, op, "La cantidad de cada material debe ser mayor a cero.");
                    }
                    Material m = materialDao.obtenerPorId(pm.getIdMaterial());
                    if (m == null || !m.isActivo()) {
                        throw new ValidacionException(MODULO, op,
                                "Uno de los materiales seleccionados no existe o está inactivo.");
                    }
                }
            }
            if (idsEmpleados != null) {
                for (Integer idEmp : idsEmpleados) {
                    if (idEmp == null || idEmp <= 0) {
                        continue;
                    }
                    Empleado e = empleadoDao.obtenerPorId(idEmp);
                    if (e == null || !e.isActivo()) {
                        throw new ValidacionException(MODULO, op,
                                "Uno de los empleados asignados no existe o está inactivo.");
                    }
                }
            }
            if (pagoInicial != null) {
                if (pagoInicial.getMonto() == null || pagoInicial.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValidacionException(MODULO, op, "El monto del anticipo/pago inicial debe ser mayor a cero.");
                }
                if (pagoInicial.getFechaPago() == null) {
                    pagoInicial.setFechaPago(LocalDate.now());
                }
                if (pagoInicial.getConcepto() == null || pagoInicial.getConcepto().isBlank()) {
                    pagoInicial.setConcepto("Anticipo inicial - " + p.getNombreProyecto());
                }
                if (pagoInicial.getEstadoCuenta() == null || pagoInicial.getEstadoCuenta().isBlank()) {
                    pagoInicial.setEstadoCuenta("Parcial");
                }
                pagoInicial.setActivo(true);
            }
            return dao.crearProyectoIntegral(p, materiales, idsEmpleados, pagoInicial, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", op, ex);
            throw ex;
        }
    }

    public Proyecto consultarPorId(int idProyecto) throws SoptecException {
        if (idProyecto <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID del proyecto debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idProyecto);
    }

    public List<Proyecto> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Proyecto> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Proyecto p) throws SoptecException {
        try {
            if (p.getIdProyecto() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PROYECTO",
                        "Selecciona primero el proyecto que deseas actualizar.");
            }
            validar(p, "ACTUALIZAR_PROYECTO");
            if (!dao.actualizar(p)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PROYECTO", "El proyecto ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "ProyectoService", "ACTUALIZAR_PROYECTO",
                    "Proyecto actualizado ID=" + p.getIdProyecto());
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "ACTUALIZAR_PROYECTO", ex);
            throw ex;
        }
    }

    public void desactivar(int idProyecto) throws SoptecException {
        try {
            if (idProyecto <= 0) {
                throw new ValidacionException(MODULO, "DESACTIVAR_PROYECTO", "Selecciona un proyecto.");
            }
            if (dao.tieneLevantamientos(idProyecto, true)) {
                throw new IntegridadReferencialException(MODULO, "DESACTIVAR_PROYECTO",
                        "No se puede desactivar el proyecto porque tiene levantamientos activos relacionados.");
            }
            if (!dao.cambiarEstadoActivo(idProyecto, false)) {
                throw new ValidacionException(MODULO, "DESACTIVAR_PROYECTO", "No se encontró el proyecto.");
            }
            AppLogger.registrarInfo(MODULO, "ProyectoService", "DESACTIVAR_PROYECTO",
                    "Proyecto desactivado ID=" + idProyecto);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "DESACTIVAR_PROYECTO", ex);
            throw ex;
        }
    }

    public void reactivar(int idProyecto) throws SoptecException {
        if (idProyecto <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_PROYECTO", "Selecciona un proyecto.");
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "REACTIVAR_PROYECTO", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idProyecto, true);
        AppLogger.registrarInfo(MODULO, "ProyectoService", "REACTIVAR_PROYECTO",
                "Proyecto reactivado ID=" + idProyecto);
    }

    public void eliminarFisico(int idProyecto) throws SoptecException {
        try {
            if (idProyecto <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_PROYECTO", "Selecciona un proyecto.");
            }
            if (dao.tieneLevantamientos(idProyecto, false) || dao.tieneOtrasRelaciones(idProyecto)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_PROYECTO",
                        "No se puede eliminar el proyecto porque tiene levantamientos u otros registros relacionados.");
            }
            if (!dao.eliminarFisico(idProyecto)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_PROYECTO", "No se encontró el proyecto.");
            }
            AppLogger.registrarInfo(MODULO, "ProyectoService", "ELIMINAR_FISICO_PROYECTO",
                    "Proyecto eliminado fisicamente ID=" + idProyecto);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoService", "ELIMINAR_FISICO_PROYECTO", ex);
            throw ex;
        }
    }

    private void validar(Proyecto p, String op) throws SoptecException {
        if (p == null) {
            throw new ValidacionException(MODULO, op, "Los datos del proyecto son obligatorios.");
        }
        p.setNombreProyecto(Validador.requerido(p.getNombreProyecto(), "Nombre del proyecto", MODULO, op));
        Validador.longitudMaxima(p.getNombreProyecto(), 100, "Nombre del proyecto", MODULO, op);
        Validador.validarRangoFechas(p.getFechaInicio(), p.getFechaEstimadaFin(), MODULO, op);
        p.setEstado(Validador.requerido(p.getEstado(), "Estado del proyecto", MODULO, op));
        Validador.longitudMaxima(p.getEstado(), 30, "Estado del proyecto", MODULO, op);
        if (p.getCostoTotal() == null || p.getCostoTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException(MODULO, op, "El costo total no puede ser negativo.");
        }
        if (p.getIdCliente() <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un cliente para el proyecto.");
        }
        Cliente cli = clienteDao.obtenerPorId(p.getIdCliente());
        if (cli == null) {
            throw new IntegridadReferencialException(MODULO, op,
                    "El cliente seleccionado no existe en la base de datos.");
        }
        if (!cli.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El cliente '" + cli.getNombreCliente() + "' está inactivo y no puede asignarse a un proyecto.");
        }
    }
}
