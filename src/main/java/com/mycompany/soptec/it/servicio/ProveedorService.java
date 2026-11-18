package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.ProveedorDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.util.List;

/**
 * Reglas de negocio para proveedores.
 */
public class ProveedorService {

    private static final String MODULO = "Proveedores";
    private final ProveedorDao dao = new ProveedorDao();

    public Proveedor crear(Proveedor p) throws SoptecException {
        try {
            validar(p, "CREAR_PROVEEDOR");
            if (dao.existeDuplicado(p.getNombreProveedor(), p.getCorreoProveedor(), null)) {
                throw new ValidacionException(MODULO, "CREAR_PROVEEDOR",
                        "Ya existe un proveedor registrado con el mismo nombre o correo electrónico.");
            }
            p.setActivo(true);
            Proveedor creado = dao.crear(p);
            AppLogger.registrarInfo(MODULO, "ProveedorService", "CREAR_PROVEEDOR",
                    "Proveedor creado ID=" + creado.getIdProveedor());
            return creado;
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "CREAR_PROVEEDOR", ex);
            throw ex;
        }
    }

    public Proveedor consultarPorId(int idProveedor) throws SoptecException {
        if (idProveedor <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID del proveedor debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idProveedor);
    }

    public List<Proveedor> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Proveedor> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Proveedor p) throws SoptecException {
        try {
            if (p.getIdProveedor() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PROVEEDOR",
                        "Selecciona primero el proveedor que deseas actualizar.");
            }
            validar(p, "ACTUALIZAR_PROVEEDOR");
            if (dao.existeDuplicado(p.getNombreProveedor(), p.getCorreoProveedor(), p.getIdProveedor())) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PROVEEDOR",
                        "Ya existe otro proveedor con el mismo nombre o correo electrónico.");
            }
            if (!dao.actualizar(p)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_PROVEEDOR", "El proveedor ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "ProveedorService", "ACTUALIZAR_PROVEEDOR",
                    "Proveedor actualizado ID=" + p.getIdProveedor());
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "ACTUALIZAR_PROVEEDOR", ex);
            throw ex;
        }
    }

    public void desactivar(int idProveedor) throws SoptecException {
        if (idProveedor <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_PROVEEDOR", "Selecciona un proveedor.");
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "DESACTIVAR_PROVEEDOR", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idProveedor, false);
        AppLogger.registrarInfo(MODULO, "ProveedorService", "DESACTIVAR_PROVEEDOR",
                "Proveedor desactivado ID=" + idProveedor);
    }

    public void reactivar(int idProveedor) throws SoptecException {
        if (idProveedor <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_PROVEEDOR", "Selecciona un proveedor.");
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "REACTIVAR_PROVEEDOR", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idProveedor, true);
        AppLogger.registrarInfo(MODULO, "ProveedorService", "REACTIVAR_PROVEEDOR",
                "Proveedor reactivado ID=" + idProveedor);
    }

    public void eliminarFisico(int idProveedor) throws SoptecException {
        try {
            if (idProveedor <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_PROVEEDOR", "Selecciona un proveedor.");
            }
            if (dao.tieneRelaciones(idProveedor)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_PROVEEDOR",
                        "No se puede eliminar el proveedor porque tiene materiales asociados.");
            }
            if (!dao.eliminarFisico(idProveedor)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_PROVEEDOR", "No se encontró el proveedor.");
            }
            AppLogger.registrarInfo(MODULO, "ProveedorService", "ELIMINAR_FISICO_PROVEEDOR",
                    "Proveedor eliminado fisicamente ID=" + idProveedor);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProveedorService", "ELIMINAR_FISICO_PROVEEDOR", ex);
            throw ex;
        }
    }

    private void validar(Proveedor p, String op) throws ValidacionException {
        if (p == null) {
            throw new ValidacionException(MODULO, op, "Los datos del proveedor son obligatorios.");
        }
        p.setNombreProveedor(Validador.requerido(p.getNombreProveedor(), "Nombre del proveedor", MODULO, op));
        Validador.longitudMaxima(p.getNombreProveedor(), 100, "Nombre del proveedor", MODULO, op);
        p.setTelefonoProveedor(Validador.validarTelefono(p.getTelefonoProveedor(), MODULO, op));
        p.setCorreoProveedor(Validador.validarCorreo(p.getCorreoProveedor(), MODULO, op));
        p.setImagen(Validador.opcional(p.getImagen()));
        Validador.longitudMaxima(p.getImagen(), 500, "Ruta de imagen", MODULO, op);
    }
}
