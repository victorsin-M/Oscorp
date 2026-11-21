package com.mycompany.soptec.it.servicio;

import com.mycompany.soptec.it.dao.MaterialDao;
import com.mycompany.soptec.it.dao.MaterialProveedorDao;
import com.mycompany.soptec.it.dao.ProveedorDao;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.MaterialProveedor;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.util.AppLogger;
import java.util.List;

/**
 * Reglas de negocio para la relacion M:N material_proveedor.
 */
public class MaterialProveedorService {

    private static final String MODULO = "MaterialProveedor";
    private final MaterialProveedorDao dao = new MaterialProveedorDao();
    private final MaterialDao materialDao = new MaterialDao();
    private final ProveedorDao proveedorDao = new ProveedorDao();

    public void asociar(int idMaterial, int idProveedor) throws SoptecException {
        try {
            validarEntidades(idMaterial, idProveedor, "ASOCIAR_MATERIAL_PROVEEDOR");
            if (dao.existe(idMaterial, idProveedor)) {
                throw new ValidacionException(MODULO, "ASOCIAR_MATERIAL_PROVEEDOR",
                        "Este proveedor ya está asociado al material seleccionado.");
            }
            dao.asociar(idMaterial, idProveedor);
            AppLogger.registrarInfo(MODULO, "MaterialProveedorService", "ASOCIAR_MATERIAL_PROVEEDOR",
                    "Asociado material ID=" + idMaterial + " con proveedor ID=" + idProveedor);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialProveedorService", "ASOCIAR_MATERIAL_PROVEEDOR", ex);
            throw ex;
        }
    }

    public List<MaterialProveedor> listarTodos() throws SoptecException {
        return dao.listarTodos();
    }

    public List<MaterialProveedor> listarPorMaterial(int idMaterial) throws SoptecException {
        return dao.listarPorMaterial(idMaterial);
    }

    public List<MaterialProveedor> listarPorProveedor(int idProveedor) throws SoptecException {
        return dao.listarPorProveedor(idProveedor);
    }

    public List<MaterialProveedor> buscar(String criterio) throws SoptecException {
        return dao.buscar(criterio);
    }

    public void actualizarAsociacion(int idMatOrig, int idProvOrig, int idMatNuevo, int idProvNuevo)
            throws SoptecException {
        try {
            validarEntidades(idMatNuevo, idProvNuevo, "ACTUALIZAR_MATERIAL_PROVEEDOR");
            if ((idMatOrig != idMatNuevo || idProvOrig != idProvNuevo) && dao.existe(idMatNuevo, idProvNuevo)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_MATERIAL_PROVEEDOR",
                        "Esa relación material-proveedor ya se encuentra registrada.");
            }
            if (!dao.actualizarAsociacion(idMatOrig, idProvOrig, idMatNuevo, idProvNuevo)) {
                throw new ValidacionException(MODULO, "ACTUALIZAR_MATERIAL_PROVEEDOR",
                        "La relación seleccionada ya no existe.");
            }
            AppLogger.registrarInfo(MODULO, "MaterialProveedorService", "ACTUALIZAR_MATERIAL_PROVEEDOR",
                    "Actualizada relacion (" + idMatOrig + "," + idProvOrig + ") -> (" + idMatNuevo + "," + idProvNuevo + ")");
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialProveedorService", "ACTUALIZAR_MATERIAL_PROVEEDOR", ex);
            throw ex;
        }
    }

    public void eliminar(int idMaterial, int idProveedor) throws SoptecException {
        if (idMaterial <= 0 || idProveedor <= 0) {
            throw new ValidacionException(MODULO, "ELIMINAR_MATERIAL_PROVEEDOR", "Selecciona una relación de la tabla.");
        }
        if (!dao.eliminar(idMaterial, idProveedor)) {
            throw new ValidacionException(MODULO, "ELIMINAR_MATERIAL_PROVEEDOR", "La relación ya no existe.");
        }
        AppLogger.registrarInfo(MODULO, "MaterialProveedorService", "ELIMINAR_MATERIAL_PROVEEDOR",
                "Eliminada relacion material ID=" + idMaterial + " proveedor ID=" + idProveedor);
    }

    public void sincronizarProveedoresDeMaterial(int idMaterial, List<Integer> idsProveedores, boolean simularFallo)
            throws SoptecException {
        try {
            Material mat = materialDao.obtenerPorId(idMaterial);
            if (mat == null || !mat.isActivo()) {
                throw new ValidacionException(MODULO, "SINCRONIZAR_PROVEEDORES",
                        "Selecciona un material activo válido.");
            }
            if (idsProveedores != null) {
                for (Integer idProv : idsProveedores) {
                    if (idProv == null || idProv <= 0) {
                        continue;
                    }
                    Proveedor prov = proveedorDao.obtenerPorId(idProv);
                    if (prov == null || !prov.isActivo()) {
                        throw new ValidacionException(MODULO, "SINCRONIZAR_PROVEEDORES",
                                "Uno de los proveedores seleccionados no existe o está inactivo.");
                    }
                }
            }
            dao.sincronizarProveedoresDeMaterial(idMaterial, idsProveedores, simularFallo);
        } catch (ValidacionException | IntegridadReferencialException ex) {
            AppLogger.registrarExcepcion(MODULO, "MaterialProveedorService", "SINCRONIZAR_PROVEEDORES", ex);
            throw ex;
        }
    }

    private void validarEntidades(int idMaterial, int idProveedor, String op) throws SoptecException {
        if (idMaterial <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un material de la lista.");
        }
        if (idProveedor <= 0) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proveedor de la lista.");
        }
        Material mat = materialDao.obtenerPorId(idMaterial);
        if (mat == null) {
            throw new IntegridadReferencialException(MODULO, op, "El material seleccionado no existe.");
        }
        if (!mat.isActivo()) {
            throw new ValidacionException(MODULO, op, "El material seleccionado está inactivo.");
        }
        Proveedor prov = proveedorDao.obtenerPorId(idProveedor);
        if (prov == null) {
            throw new IntegridadReferencialException(MODULO, op, "El proveedor seleccionado no existe.");
        }
        if (!prov.isActivo()) {
            throw new ValidacionException(MODULO, op, "El proveedor seleccionado está inactivo.");
        }
    }
}
