package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.CitaDao;
import com.mycompany.soptec.it.dao.ClienteDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio para citas asociadas a clientes y levantamiento tecnico conjunto.
 */
public class CitaService {

    private static final String MODULO = "Citas";
    private final CitaDao dao = new CitaDao();
    private final ClienteDao clienteDao = new ClienteDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();

    public Cita crear(Cita c) throws SoptecException {
        try {
            validar(c, "CREAR_CITA");
            c.setActivo(true);
            Cita creada = dao.crear(c);
            AppLogger.registrarInfo(MODULO, "CitaService", "CREAR_CITA",
                    "Cita registrada ID=" + creada.getIdCita());
            return creada;
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "CitaService", "CREAR_CITA", ex);
            throw ex;
        }
    }

    /**
     * Registra una cita y su levantamiento/servicio inicial en una transaccion atomica.
     * Si falla el levantamiento relacionado, la cita no queda registrada parcialmente.
     */
    public Cita crearConLevantamientoInicial(Cita c, Levantamiento lev, boolean simularFallo) throws SoptecException {
        String op = "CREAR_CITA_CON_LEVANTAMIENTO";
        try {
            validar(c, op);
            c.setActivo(true);
            if (lev != null) {
                if (lev.getIdProyecto() <= 0) {
                    throw new ValidacionException(MODULO, op,
                            "Para registrar un levantamiento junto con la cita, selecciona un proyecto válido.");
                }
                Proyecto p = proyectoDao.obtenerPorId(lev.getIdProyecto());
                if (p == null) {
                    throw new IntegridadReferencialException(MODULO, op,
                            "El proyecto seleccionado para el levantamiento no existe.");
                }
                if (!p.isActivo()) {
                    throw new ValidacionException(MODULO, op,
                            "El proyecto '" + p.getNombreProyecto() + "' está inactivo.");
                }
                if (lev.getFechaVisita() == null) {
                    lev.setFechaVisita(c.getFechaCita());
                }
                lev.setObservacionesTecnicas(Validador.opcional(lev.getObservacionesTecnicas()));
            }
            return dao.crearConLevantamientoInicial(c, lev, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "CitaService", op, ex);
            throw ex;
        }
    }

    public Cita consultarPorId(int idCita) throws SoptecException {
        if (idCita <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID de la cita debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "CitaService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idCita);
    }

    public List<Cita> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Cita> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Cita c) throws SoptecException {
        try {
            if (c.getIdCita() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CITA",
                        "Selecciona primero la cita que deseas actualizar.");
            }
            validar(c, "ACTUALIZAR_CITA");
            if (!dao.actualizar(c)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CITA", "La cita ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "CitaService", "ACTUALIZAR_CITA",
                    "Cita actualizada ID=" + c.getIdCita());
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "CitaService", "ACTUALIZAR_CITA", ex);
            throw ex;
        }
    }

    public void desactivar(int idCita) throws SoptecException {
        if (idCita <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_CITA", "Selecciona una cita.");
            AppLogger.registrarExcepcion(MODULO, "CitaService", "DESACTIVAR_CITA", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idCita, false);
        AppLogger.registrarInfo(MODULO, "CitaService", "DESACTIVAR_CITA", "Cita desactivada ID=" + idCita);
    }

    public void reactivar(int idCita) throws SoptecException {
        if (idCita <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_CITA", "Selecciona una cita.");
            AppLogger.registrarExcepcion(MODULO, "CitaService", "REACTIVAR_CITA", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idCita, true);
        AppLogger.registrarInfo(MODULO, "CitaService", "REACTIVAR_CITA", "Cita reactivada ID=" + idCita);
    }

    public void eliminarFisico(int idCita) throws SoptecException {
        try {
            if (idCita <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_CITA", "Selecciona una cita.");
            }
            if (dao.tieneLevantamientos(idCita)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_CITA",
                        "No se puede eliminar la cita porque está vinculada a un levantamiento técnico.");
            }
            if (!dao.eliminarFisico(idCita)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_CITA", "No se encontró la cita.");
            }
            AppLogger.registrarInfo(MODULO, "CitaService", "ELIMINAR_FISICO_CITA",
                    "Cita eliminada fisicamente ID=" + idCita);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "CitaService", "ELIMINAR_FISICO_CITA", ex);
            throw ex;
        }
    }

    private void validar(Cita c, String op) throws SoptecException {
        if (c == null) {
            throw new ValidacionException(MODULO, op, "Los datos de la cita son obligatorios.");
        }
        if (c.getFechaCita() == null) {
            throw new ValidacionException(MODULO, op, "La fecha de la cita es obligatoria.");
        }
        if (c.getHoraCita() == null) {
            throw new ValidacionException(MODULO, op, "La hora de la cita es obligatoria.");
        }
        c.setEstadoCita(Validador.requerido(c.getEstadoCita(), "Estado de la cita", MODULO, op));
        Validador.longitudMaxima(c.getEstadoCita(), 30, "Estado de la cita", MODULO, op);
        c.setObservaciones(Validador.opcional(c.getObservaciones()));
        if (c.getIdCliente() <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un cliente para agendar la cita.");
        }
        Cliente cli = clienteDao.obtenerPorId(c.getIdCliente());
        if (cli == null) {
            throw new IntegridadReferencialException(MODULO, op,
                    "El cliente seleccionado no existe en la base de datos.");
        }
        if (!cli.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El cliente '" + cli.getNombreCliente() + "' está inactivo y no se le pueden asignar citas.");
        }
    }
}
