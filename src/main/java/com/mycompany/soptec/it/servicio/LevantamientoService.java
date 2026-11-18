package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.CitaDao;
import com.mycompany.soptec.it.dao.LevantamientoDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio para levantamientos de proyectos.
 */
public class LevantamientoService {

    private static final String MODULO = "Levantamientos";
    private final LevantamientoDao dao = new LevantamientoDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final CitaDao citaDao = new CitaDao();

    public Levantamiento crear(Levantamiento l) throws SoptecException {
        try {
            validar(l, "CREAR_LEVANTAMIENTO");
            l.setActivo(true);
            Levantamiento creado = dao.crear(l);
            AppLogger.registrarInfo(MODULO, "LevantamientoService", "CREAR_LEVANTAMIENTO",
                    "Levantamiento creado ID=" + creado.getIdLevantamiento());
            return creado;
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "CREAR_LEVANTAMIENTO", ex);
            throw ex;
        }
    }

    public Levantamiento consultarPorId(int idLevantamiento) throws SoptecException {
        if (idLevantamiento <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID del levantamiento debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idLevantamiento);
    }

    public List<Levantamiento> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Levantamiento> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Levantamiento l) throws SoptecException {
        try {
            if (l.getIdLevantamiento() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_LEVANTAMIENTO",
                        "Selecciona primero el levantamiento que deseas actualizar.");
            }
            validar(l, "ACTUALIZAR_LEVANTAMIENTO");
            if (!dao.actualizar(l)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_LEVANTAMIENTO", "El levantamiento ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "LevantamientoService", "ACTUALIZAR_LEVANTAMIENTO",
                    "Levantamiento actualizado ID=" + l.getIdLevantamiento());
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "ACTUALIZAR_LEVANTAMIENTO", ex);
            throw ex;
        }
    }

    public void desactivar(int idLevantamiento) throws SoptecException {
        if (idLevantamiento <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_LEVANTAMIENTO",
                    "Selecciona un levantamiento.");
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "DESACTIVAR_LEVANTAMIENTO", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idLevantamiento, false);
        AppLogger.registrarInfo(MODULO, "LevantamientoService", "DESACTIVAR_LEVANTAMIENTO",
                "Levantamiento desactivado ID=" + idLevantamiento);
    }

    public void reactivar(int idLevantamiento) throws SoptecException {
        if (idLevantamiento <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_LEVANTAMIENTO",
                    "Selecciona un levantamiento.");
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "REACTIVAR_LEVANTAMIENTO", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idLevantamiento, true);
        AppLogger.registrarInfo(MODULO, "LevantamientoService", "REACTIVAR_LEVANTAMIENTO",
                "Levantamiento reactivado ID=" + idLevantamiento);
    }

    public void eliminarFisico(int idLevantamiento) throws SoptecException {
        if (idLevantamiento <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "ELIMINAR_FISICO_LEVANTAMIENTO",
                    "Selecciona un levantamiento.");
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "ELIMINAR_FISICO_LEVANTAMIENTO", ex);
            throw ex;
        }
        if (!dao.eliminarFisico(idLevantamiento)) {
            ValidacionException ex = new ValidacionException(MODULO, "ELIMINAR_FISICO_LEVANTAMIENTO",
                    "No se encontró el levantamiento.");
            AppLogger.registrarExcepcion(MODULO, "LevantamientoService", "ELIMINAR_FISICO_LEVANTAMIENTO", ex);
            throw ex;
        }
        AppLogger.registrarInfo(MODULO, "LevantamientoService", "ELIMINAR_FISICO_LEVANTAMIENTO",
                "Levantamiento eliminado fisicamente ID=" + idLevantamiento);
    }

    private void validar(Levantamiento l, String op) throws SoptecException {
        if (l == null) {
            throw new ValidacionException(MODULO, op, "Los datos del levantamiento son obligatorios.");
        }
        if (l.getFechaVisita() == null) {
            throw new ValidacionException(MODULO, op, "La fecha de visita es obligatoria.");
        }
        l.setCroquis(Validador.opcional(l.getCroquis()));
        l.setObservacionesTecnicas(Validador.opcional(l.getObservacionesTecnicas()));

        if (l.getIdProyecto() <= 0) {
            throw new ValidacionException(MODULO, op,
                    "Debes seleccionar un proyecto existente para asociar el levantamiento.");
        }
        Proyecto proy = proyectoDao.obtenerPorId(l.getIdProyecto());
        if (proy == null) {
            throw new IntegridadReferencialException(MODULO, op,
                    "El proyecto seleccionado no existe en la base de datos.");
        }
        if (!proy.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El proyecto '" + proy.getNombreProyecto() + "' está inactivo y no admite nuevos levantamientos.");
        }

        if (l.getIdCita() != null && l.getIdCita() > 0) {
            Cita cita = citaDao.obtenerPorId(l.getIdCita());
            if (cita == null) {
                throw new IntegridadReferencialException(MODULO, op,
                        "La cita opcional seleccionada no existe en la base de datos.");
            }
            if (!cita.isActivo()) {
                throw new ValidacionException(MODULO, op,
                        "La cita seleccionada está inactiva.");
            }
        }
    }
}
