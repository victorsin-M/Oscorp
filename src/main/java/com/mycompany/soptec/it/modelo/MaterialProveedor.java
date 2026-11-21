package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;

/**
 * Modelo de la tabla puente material_proveedor (relacion M:N Material <-> Proveedor).
 */
public class MaterialProveedor {

    private int idMaterial;
    private int idProveedor;

    // Campos descriptivos obtenidos mediante JOIN
    private String descripcionMaterial;
    private BigDecimal precioCosto = BigDecimal.ZERO;
    private int existencias;
    private String nombreProveedor;
    private String telefonoProveedor;
    private String correoProveedor;

    public MaterialProveedor() {
    }

    public MaterialProveedor(int idMaterial, int idProveedor) {
        this.idMaterial = idMaterial;
        this.idProveedor = idProveedor;
    }

    public MaterialProveedor(int idMaterial, int idProveedor, String descripcionMaterial,
            BigDecimal precioCosto, int existencias, String nombreProveedor,
            String telefonoProveedor, String correoProveedor) {
        this.idMaterial = idMaterial;
        this.idProveedor = idProveedor;
        this.descripcionMaterial = descripcionMaterial;
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
        this.existencias = existencias;
        this.nombreProveedor = nombreProveedor;
        this.telefonoProveedor = telefonoProveedor;
        this.correoProveedor = correoProveedor;
    }

    public int getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getDescripcionMaterial() {
        return descripcionMaterial;
    }

    public void setDescripcionMaterial(String descripcionMaterial) {
        this.descripcionMaterial = descripcionMaterial;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto != null ? precioCosto : BigDecimal.ZERO;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getTelefonoProveedor() {
        return telefonoProveedor;
    }

    public void setTelefonoProveedor(String telefonoProveedor) {
        this.telefonoProveedor = telefonoProveedor;
    }

    public String getCorreoProveedor() {
        return correoProveedor;
    }

    public void setCorreoProveedor(String correoProveedor) {
        this.correoProveedor = correoProveedor;
    }

    @Override
    public String toString() {
        return (descripcionMaterial != null ? descripcionMaterial : "Material #" + idMaterial)
                + " <-> " + (nombreProveedor != null ? nombreProveedor : "Proveedor #" + idProveedor);
    }
}
