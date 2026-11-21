package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.MaterialDao;
import com.mycompany.soptec.it.dao.ProyectoDao;
import com.mycompany.soptec.it.dao.ProyectoMaterialDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio para la relacion M:N proyecto_material con control de inventario.
 */
public class ProyectoMaterialService {

    private static final String MODULO = "ProyectoMaterial";
    private final ProyectoMaterialDao dao = new ProyectoMaterialDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final MaterialDao materialDao = new MaterialDao();

    public void asignarConDescuentoStock(int idProyecto, int idMaterial, BigDecimal cantidad, boolean simularFallo)
            throws SoptecException {
        try {
            validarDatos(idProyecto, idMaterial, cantidad, "ASIGNAR_MATERIAL_PROYECTO");
            dao.asignarConDescuentoStock(idProyecto, idMaterial, cantidad, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoMaterialService", "ASIGNAR_MATERIAL_PROYECTO", ex);
            throw ex;
        }
    }

    public void asignarSinDescuento(int idProyecto, int idMaterial, BigDecimal cantidad) throws SoptecException {
        try {
            validarDatos(idProyecto, idMaterial, cantidad, "ASIGNAR_MATERIAL");
            dao.asignar(idProyecto, idMaterial, cantidad);
            AppLogger.registrarInfo(MODULO, "ProyectoMaterialService", "ASIGNAR_MATERIAL",
                    "Material ID=" + idMaterial + " asignado a proyecto ID=" + idProyecto + " cant=" + cantidad);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoMaterialService", "ASIGNAR_MATERIAL", ex);
            throw ex;
        }
    }

    public ProyectoMaterial consultar(int idProyecto, int idMaterial) throws SoptecException {
        return dao.obtener(idProyecto, idMaterial);
    }

    public List<ProyectoMaterial> listarTodos() throws SoptecException {
        return dao.listarTodos();
    }

    public List<ProyectoMaterial> listarPorProyecto(int idProyecto) throws SoptecException {
        if (idProyecto <= 0) {
            throw new ValidacionException(MODULO, "LISTAR_POR_PROYECTO", "Selecciona un proyecto válido.");
        }
        return dao.listarPorProyecto(idProyecto);
    }

    public List<ProyectoMaterial> buscar(String criterio) throws SoptecException {
        return dao.buscar(criterio);
    }

    public void actualizarCantidad(int idProyecto, int idMaterial, BigDecimal nuevaCantidad) throws SoptecException {
        try {
            validarDatos(idProyecto, idMaterial, nuevaCantidad, "ACTUALIZAR_CANTIDAD");
            if (!dao.actualizarCantidad(idProyecto, idMaterial, nuevaCantidad)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_CANTIDAD",
                        "No se encontró el material asignado al proyecto.");
            }
            AppLogger.registrarInfo(MODULO, "ProyectoMaterialService", "ACTUALIZAR_CANTIDAD",
                    "Cantidad actualizada proyecto=" + idProyecto + " material=" + idMaterial + " cant=" + nuevaCantidad);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "ProyectoMaterialService", "ACTUALIZAR_CANTIDAD", ex);
            throw ex;
        }
    }

    public void eliminar(int idProyecto, int idMaterial, boolean reintegrarStock) throws SoptecException {
        if (idProyecto <= 0 || idMaterial <= 0) {
            throw new ValidacionException(MODULO, "ELIMINAR_MATERIAL_PROYECTO", "Selecciona un registro de la tabla.");
        }
        boolean ok = reintegrarStock
                ? dao.eliminarConReintegroStock(idProyecto, idMaterial)
                : dao.eliminar(idProyecto, idMaterial);
        if (!ok) {
            throw new ValidacionException(MODULO, "ELIMINAR_MATERIAL_PROYECTO", "El registro ya no existe.");
        }
        AppLogger.registrarInfo(MODULO, "ProyectoMaterialService", "ELIMINAR_MATERIAL_PROYECTO",
                "Material ID=" + idMaterial + " retirado del proyecto ID=" + idProyecto
                        + " (reintegrarStock=" + reintegrarStock + ")");
    }

    private void validarDatos(int idProyecto, int idMaterial, BigDecimal cantidad, String op) throws SoptecException {
        if (idProyecto <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proyecto de la lista.");
        }
        if (idMaterial <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un material de la lista.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException(MODULO, op, "La cantidad debe ser mayor a cero.");
        }
        Proyecto proy = proyectoDao.obtenerPorId(idProyecto);
        if (proy == null) {
            throw new IntegridadReferencialException(MODULO, op, "El proyecto seleccionado no existe.");
        }
        if (!proy.isActivo()) {
            throw new ValidacionException(MODULO, op, "El proyecto seleccionado está inactivo.");
        }
        Material mat = materialDao.obtenerPorId(idMaterial);
        if (mat == null) {
            throw new IntegridadReferencialException(MODULO, op, "El material seleccionado no existe.");
        }
        if (!mat.isActivo()) {
            throw new ValidacionException(MODULO, op,
                    "El material '" + mat.getDescripcionMaterial() + "' está inactivo.");
        }
    }
}
