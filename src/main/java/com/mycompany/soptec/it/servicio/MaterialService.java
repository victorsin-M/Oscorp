package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.MaterialDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio para materiales.
 */
public class MaterialService {

    private static final String MODULO = "Materiales";
    private final MaterialDao dao = new MaterialDao();

    public Material crear(Material m) throws SoptecException {
        try {
            validar(m, "CREAR_MATERIAL");
            if (dao.existeDuplicado(m.getDescripcionMaterial(), null)) {
                throw new ValidacionException(MODULO, "CREAR_MATERIAL",
                        "Ya existe un material registrado con la misma descripción.");
            }
            m.setActivo(true);
            Material creado = dao.crear(m);
            AppLogger.registrarInfo(MODULO, "MaterialService", "CREAR_MATERIAL",
                    "Material creado ID=" + creado.getIdMaterial());
            return creado;
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "CREAR_MATERIAL", ex);
            throw ex;
        }
    }

    public Material consultarPorId(int idMaterial) throws SoptecException {
        if (idMaterial <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "CONSULTAR_POR_ID",
                    "El ID del material debe ser mayor a cero.");
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "CONSULTAR_POR_ID", ex);
            throw ex;
        }
        return dao.obtenerPorId(idMaterial);
    }

    public List<Material> listar(boolean incluirInactivos) throws SoptecException {
        return dao.listarTodos(incluirInactivos);
    }

    public List<Material> buscar(String criterio, boolean incluirInactivos) throws SoptecException {
        return dao.buscar(criterio, incluirInactivos);
    }

    public void actualizar(Material m) throws SoptecException {
        try {
            if (m.getIdMaterial() <= 0) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_MATERIAL",
                        "Selecciona primero el material que deseas actualizar.");
            }
            validar(m, "ACTUALIZAR_MATERIAL");
            if (dao.existeDuplicado(m.getDescripcionMaterial(), m.getIdMaterial())) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_MATERIAL",
                        "Ya existe otro material con la misma descripción.");
            }
            if (!dao.actualizar(m)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_MATERIAL", "El material ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "MaterialService", "ACTUALIZAR_MATERIAL",
                    "Material actualizado ID=" + m.getIdMaterial());
        } catch (ValidacionException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "ACTUALIZAR_MATERIAL", ex);
            throw ex;
        }
    }

    public void desactivar(int idMaterial) throws SoptecException {
        if (idMaterial <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "DESACTIVAR_MATERIAL", "Selecciona un material.");
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "DESACTIVAR_MATERIAL", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idMaterial, false);
        AppLogger.registrarInfo(MODULO, "MaterialService", "DESACTIVAR_MATERIAL",
                "Material desactivado ID=" + idMaterial);
    }

    public void reactivar(int idMaterial) throws SoptecException {
        if (idMaterial <= 0) {
            ValidacionException ex = new ValidacionException(MODULO, "REACTIVAR_MATERIAL", "Selecciona un material.");
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "REACTIVAR_MATERIAL", ex);
            throw ex;
        }
        dao.cambiarEstadoActivo(idMaterial, true);
        AppLogger.registrarInfo(MODULO, "MaterialService", "REACTIVAR_MATERIAL",
                "Material reactivado ID=" + idMaterial);
    }

    public void eliminarFisico(int idMaterial) throws SoptecException {
        try {
            if (idMaterial <= 0) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_MATERIAL", "Selecciona un material.");
            }
            if (dao.tieneRelaciones(idMaterial)) {
                throw new IntegridadReferencialException(MODULO, "ELIMINAR_FISICO_MATERIAL",
                        "No se puede eliminar el material porque está relacionado con proveedores, proyectos u órdenes.");
            }
            if (!dao.eliminarFisico(idMaterial)) {
                throw new ValidacionException(MODULO, "ELIMINAR_FISICO_MATERIAL", "No se encontró el material.");
            }
            AppLogger.registrarInfo(MODULO, "MaterialService", "ELIMINAR_FISICO_MATERIAL",
                    "Material eliminado fisicamente ID=" + idMaterial);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialService", "ELIMINAR_FISICO_MATERIAL", ex);
            throw ex;
        }
    }

    private void validar(Material m, String op) throws ValidacionException {
        if (m == null) {
            throw new ValidacionException(MODULO, op, "Los datos del material son obligatorios.");
        }
        m.setDescripcionMaterial(Validador.requerido(m.getDescripcionMaterial(), "Descripción del material", MODULO, op));
        Validador.longitudMaxima(m.getDescripcionMaterial(), 150, "Descripción del material", MODULO, op);
        if (m.getPrecioCosto() == null || m.getPrecioCosto().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException(MODULO, op, "El precio de costo no puede ser negativo.");
        }
        if (m.getExistencias() < 0) {
            throw new ValidacionException(MODULO, op, "Las existencias no pueden ser negativas.");
        }
    }
}
