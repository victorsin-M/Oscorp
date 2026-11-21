package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;

/**
 * Modelo de la tabla puente orden_compra_material (relacion M:N OrdenCompra <-> Material).
 */
public class OrdenCompraMaterial {

    private int idOrdenCompra;
    private int idMaterial;
    private BigDecimal cantidad = BigDecimal.ONE;

    // Campos descriptivos obtenidos mediante JOIN
    private String descripcionMaterial;
    private BigDecimal precioCosto = BigDecimal.ZERO;

    public OrdenCompraMaterial() {
    }

    public OrdenCompraMaterial(int idOrdenCompra, int idMaterial, BigDecimal cantidad) {
        this.idOrdenCompra = idOrdenCompra;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
    }

    public OrdenCompraMaterial(int idOrdenCompra, int idMaterial, BigDecimal cantidad,
            String descripcionMaterial, BigDecimal precioCosto) {
        this.idOrdenCompra = idOrdenCompra;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.descripcionMaterial = descripcionMaterial;
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
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

    public BigDecimal getImporte() {
        return getPrecioCosto().multiply(getCantidad());
    }

    @Override
    public String toString() {
        return (descripcionMaterial != null ? descripcionMaterial : "Material #" + idMaterial)
                + " x " + getCantidad().stripTrailingZeros().toPlainString();
    }
}
