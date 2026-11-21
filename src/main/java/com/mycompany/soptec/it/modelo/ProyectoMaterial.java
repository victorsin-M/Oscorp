package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;

/**
 * Modelo de la tabla puente proyecto_material (relacion M:N Proyecto <-> Material con cantidad).
 */
public class ProyectoMaterial {

    private int idProyecto;
    private int idMaterial;
    private BigDecimal cantidad = BigDecimal.ONE;

    // Campos descriptivos obtenidos mediante JOIN
    private String nombreProyecto;
    private String descripcionMaterial;
    private BigDecimal precioCosto = BigDecimal.ZERO;
    private int existenciasActuales;

    public ProyectoMaterial() {
    }

    public ProyectoMaterial(int idProyecto, int idMaterial, BigDecimal cantidad) {
        this.idProyecto = idProyecto;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
    }

    public ProyectoMaterial(int idProyecto, int idMaterial, BigDecimal cantidad, String nombreProyecto,
            String descripcionMaterial, BigDecimal precioCosto, int existenciasActuales) {
        this.idProyecto = idProyecto;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.nombreProyecto = nombreProyecto;
        this.descripcionMaterial = descripcionMaterial;
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
        this.existenciasActuales = existenciasActuales;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public int getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
    }

    public BigDecimal getCantidad() {
        return cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
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

    public int getExistenciasActuales() {
        return existenciasActuales;
    }

    public void setExistenciasActuales(int existenciasActuales) {
        this.existenciasActuales = existenciasActuales;
    }

    public BigDecimal getSubtotal() {
        return getPrecioCosto().multiply(getCantidad());
    }

    public String getProyectoDescriptivo() {
        if (nombreProyecto == null || nombreProyecto.isBlank()) {
            return "PRY-" + String.format("%04d", idProyecto);
        }
        return "PRY-" + String.format("%04d", idProyecto) + " · " + nombreProyecto;
    }

    @Override
    public String toString() {
        return (descripcionMaterial != null ? descripcionMaterial : "Material #" + idMaterial)
                + " x " + getCantidad().stripTrailingZeros().toPlainString();
    }
}
