package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;

/**
 * Modelo de la entidad base material (tabla material).
 */
public class Material {

    private int idMaterial;
    private String codigo;
    private String descripcionMaterial;
    private BigDecimal precioCosto = BigDecimal.ZERO;
    private int existencias;
    private boolean activo = true;

    public Material() {
    }

    public Material(int idMaterial, String codigo, String nombre, double precioUnitario, int stockActual) {
        this.idMaterial = idMaterial;
        this.codigo = codigo;
        this.descripcionMaterial = nombre;
        this.precioCosto = BigDecimal.valueOf(precioUnitario);
        this.existencias = stockActual;
    }

    public Material(int idMaterial, String descripcionMaterial, BigDecimal precioCosto, int existencias, boolean activo) {
        this.idMaterial = idMaterial;
        this.descripcionMaterial = descripcionMaterial;
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
        this.existencias = existencias;
        this.activo = activo;
    }

    public int getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcionMaterial() {
        return descripcionMaterial;
    }

    public void setDescripcionMaterial(String descripcionMaterial) {
        this.descripcionMaterial = descripcionMaterial;
    }

    public String getNombre() {
        return descripcionMaterial;
    }

    public void setNombre(String nombre) {
        this.descripcionMaterial = nombre;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto != null ? precioCosto : BigDecimal.ZERO;
    }

    public double getPrecioUnitario() {
        return precioCosto.doubleValue();
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public int getStockActual() {
        return existencias;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    public void actualizarPrecio(double nuevoPrecio) {
        this.precioCosto = BigDecimal.valueOf(nuevoPrecio);
    }

    public double calcularSubtotal(int cantidad) {
        return cantidad * getPrecioUnitario();
    }

    public String obtenerDetalles() {
        return descripcionMaterial + (codigo != null ? " (" + codigo + ")" : "") + " - $" + precioCosto;
    }

    @Override
    public String toString() {
        return descripcionMaterial + " (Stock: " + existencias + ")";
    }
}
