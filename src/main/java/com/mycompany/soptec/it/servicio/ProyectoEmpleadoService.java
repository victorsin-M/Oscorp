package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.EmpleadoDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.dao.ProyectoEmpleadoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoEmpleado;
import com.mycompany.soptec.it.util.AppLogger;
import java.util.List;

/**
 * Reglas de negocio para la relacion M:N proyecto_empleado.
 */
public class ProyectoEmpleadoService {

    private static final String MODULO = "ProyectoEmpleado";
    private final ProyectoEmpleadoDao dao = new ProyectoEmpleadoDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final EmpleadoDao empleadoDao = new EmpleadoDao();

    public void asignar(int idProyecto, int idEmpleado) throws SoptecException {
        try {
            validarEntidades(idProyecto, idEmpleado, "ASIGNAR_EMPLEADO");
            if (dao.existe(idProyecto, idEmpleado)) {
                throw new ValidacionException(MODULO, "ASIGNAR_EMPLEADO",
                        "Este empleado ya está asignado al proyecto seleccionado.");
            }
            dao.asignar(idProyecto, idEmpleado);
            AppLogger.registrarInfo(MODULO, "ProyectoEmpleadoService", "ASIGNAR_EMPLEADO",
                    "Asignado empleado ID=" + idEmpleado + " a proyecto ID=" + idProyecto);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoEmpleadoService", "ASIGNAR_EMPLEADO", ex);
            throw ex;
        }
    }

    public List<ProyectoEmpleado> listarTodos() throws SoptecException {
        return dao.listarTodos();
    }

    public List<ProyectoEmpleado> listarPorProyecto(int idProyecto) throws SoptecException {
        if (idProyecto <= 0) {
            throw new ValidacionException(MODULO, "LISTAR_POR_PROYECTO", "Selecciona un proyecto válido.");
        }
        return dao.listarPorProyecto(idProyecto);
    }

    public List<ProyectoEmpleado> buscar(String criterio) throws SoptecException {
        return dao.buscar(criterio);
    }

    public void actualizarAsignacion(int idProyectoOrig, int idEmpleadoOrig,
            int idProyectoNuevo, int idEmpleadoNuevo) throws SoptecException {
        try {
            validarEntidades(idProyectoNuevo, idEmpleadoNuevo, "ACTUALIZAR_ASIGNACION");
            if ((idProyectoOrig != idProyectoNuevo || idEmpleadoOrig != idEmpleadoNuevo)
                    && dao.existe(idProyectoNuevo, idEmpleadoNuevo)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_ASIGNACION",
                        "Esa combinación de proyecto y empleado ya se encuentra registrada.");
            }
            if (!dao.actualizarAsignacion(idProyectoOrig, idEmpleadoOrig, idProyectoNuevo, idEmpleadoNuevo)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_ASIGNACION",
                        "La asignación seleccionada ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "ProyectoEmpleadoService", "ACTUALIZAR_ASIGNACION",
                    "Actualizada asignacion (" + idProyectoOrig + "," + idEmpleadoOrig + ") -> ("
                            + idProyectoNuevo + "," + idEmpleadoNuevo + ")");
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoEmpleadoService", "ACTUALIZAR_ASIGNACION", ex);
            throw ex;
        }
    }

    public void eliminar(int idProyecto, int idEmpleado) throws SoptecException {
        if (idProyecto <= 0 || idEmpleado <= 0) {
            throw new ValidacionException(MODULO, "ELIMINAR_ASIGNACION", "Selecciona una asignación de la tabla.");
        }
        if (!dao.eliminar(idProyecto, idEmpleado)) {
            throw new ValidacionException(MODULO, "ELIMINAR_ASIGNACION", "La asignación ya no existe.");
        }
        AppLogger.registrarInfo(MODULO, "ProyectoEmpleadoService", "ELIMINAR_ASIGNACION",
                "Desvinculado empleado ID=" + idEmpleado + " del proyecto ID=" + idProyecto);
    }

    public void reemplazarEmpleadosDeProyecto(int idProyecto, List<Integer> idsEmpleados, boolean simularFallo)
            throws SoptecException {
        try {
            Proyecto proy = proyectoDao.obtenerPorId(idProyecto);
            if (proy == null) {
                throw new IntegridadReferencialException(MODULO, "SINCRONIZAR_EMPLEADOS",
                        "El proyecto seleccionado no existe.");
            }
            if (idsEmpleados != null) {
                for (Integer idEmp : idsEmpleados) {
                    if (idEmp == null || idEmp <= 0) {
                        continue;
                    }
                    Empleado emp = empleadoDao.obtenerPorId(idEmp);
                    if (emp == null || !emp.isActivo()) {
                        throw new ValidacionException(MODULO, "SINCRONIZAR_EMPLEADOS",
                                "Uno de los empleados seleccionados no existe o está inactivo.");
                    }
                }
            }
            dao.reemplazarEmpleadosDeProyecto(idProyecto, idsEmpleados, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoEmpleadoService", "SINCRONIZAR_EMPLEADOS", ex);
            throw ex;
        }
    }

    private void validarEntidades(int idProyecto, int idEmpleado, String op) throws SoptecException {
        if (idProyecto <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proyecto de la lista.");
        }
        if (idEmpleado <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un empleado de la lista.");
        }
        Proyecto proy = proyectoDao.obtenerPorId(idProyecto);
        if (proy == null) {
            throw new IntegridadReferencialException(MODULO, op, "El proyecto seleccionado no existe.");
        }
        if (!proy.isActivo()) {
            throw new ValidacionException(MODULO, op, "El proyecto seleccionado está inactivo.");
        }
        Empleado emp = empleadoDao.obtenerPorId(idEmpleado);
        if (emp == null) {
            throw new IntegridadReferencialException(MODULO, op, "El empleado seleccionado no existe.");
        }
        if (!emp.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El empleado '" + emp.getNombre() + "' está inactivo y no puede asignarse.");
        }
    }
}
