package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.EmpleadoDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio para empleados.
 */
public class EmpleadoService {

    private static final String MODULO = "Empleados";
    private final EmpleadoDao dao = new EmpleadoDao();

    public Empleado crear(Empleado e) throws SoptecException {
        try {
            validar(e, "CREAR_EMPLEADO");
            if (dao.existeDuplicado(e.getCorreo(), null)) {
                throw new ValidacionException(MODULO, "CREAR_EMPLEADO",
                        "Ya existe un empleado registrado con ese correo electrónico.");
            }
            e.setActivo(true);
            Empleado creado = dao.crear(e);
            AppLogger.registrarInfo(MODULO, "EmpleadoService", "CREAR_EMPLEADO",
                    "Empleado creado ID=" + creado.getIdEmpleado());
            return creado;
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "CREAR_EMPLEADO", ex);
            throw ex;
        }
    }

    public Empleado consultarPorId(int idEmpleado) throws SoptecException {
        if (idEmpleado <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID de empleado debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idEmpleado);
    }

    public List<Empleado> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Empleado> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Empleado e) throws SoptecException {
        try {
            if (e.getIdEmpleado() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_EMPLEADO",
                        "Selecciona primero el empleado que deseas actualizar.");
            }
            validar(e, "ACTUALIZAR_EMPLEADO");
            if (dao.existeDuplicado(e.getCorreo(), e.getIdEmpleado())) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_EMPLEADO",
                        "Ya existe otro empleado con el mismo correo electrónico.");
            }
            if (!dao.actualizar(e)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_EMPLEADO", "El empleado ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "EmpleadoService", "ACTUALIZAR_EMPLEADO",
                    "Empleado actualizado ID=" + e.getIdEmpleado());
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "ACTUALIZAR_EMPLEADO", ex);
            throw ex;
        }
    }

    public void desactivar(int idEmpleado) throws SoptecException {
        if (idEmpleado <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_EMPLEADO", "Selecciona un empleado.");
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "DESACTIVAR_EMPLEADO", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idEmpleado, false);
        AppLogger.registrarInfo(MODULO, "EmpleadoService", "DESACTIVAR_EMPLEADO",
                "Empleado desactivado ID=" + idEmpleado);
    }

    public void reactivar(int idEmpleado) throws SoptecException {
        if (idEmpleado <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_EMPLEADO", "Selecciona un empleado.");
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "REACTIVAR_EMPLEADO", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idEmpleado, true);
        AppLogger.registrarInfo(MODULO, "EmpleadoService", "REACTIVAR_EMPLEADO",
                "Empleado reactivado ID=" + idEmpleado);
    }

    public void eliminarFisico(int idEmpleado) throws SoptecException {
        try {
            if (idEmpleado <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_EMPLEADO", "Selecciona un empleado.");
            }
            if (dao.tieneRelaciones(idEmpleado)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_EMPLEADO",
                        "No se puede eliminar el empleado porque tiene proyectos relacionados.");
            }
            if (!dao.eliminarFisico(idEmpleado)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_EMPLEADO", "No se encontró el empleado.");
            }
            AppLogger.registrarInfo(MODULO, "EmpleadoService", "ELIMINAR_FISICO_EMPLEADO",
                    "Empleado eliminado fisicamente ID=" + idEmpleado);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "EmpleadoService", "ELIMINAR_FISICO_EMPLEADO", ex);
            throw ex;
        }
    }

    private void validar(Empleado e, String op) throws ValidacionException {
        if (e == null) {
            throw new ValidacionException(MODULO, op, "Los datos del empleado son obligatorios.");
        }
        e.setNombre(Validador.requerido(e.getNombre(), "Nombre", MODULO, op));
        Validador.longitudMaxima(e.getNombre(), 100, "Nombre", MODULO, op);
        e.setTelefono(Validador.validarTelefono(e.getTelefono(), MODULO, op));
        e.setPuesto(Validador.requerido(e.getPuesto(), "Puesto", MODULO, op));
        Validador.longitudMaxima(e.getPuesto(), 50, "Puesto", MODULO, op);
        e.setCorreo(Validador.validarCorreo(e.getCorreo(), MODULO, op));
        e.setImagen(Validador.opcional(e.getImagen()));
        Validador.longitudMaxima(e.getImagen(), 500, "Ruta de imagen", MODULO, op);
    }
}
