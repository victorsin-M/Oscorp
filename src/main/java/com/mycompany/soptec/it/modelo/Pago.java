package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de la tabla pago (Proyecto 1:N Pago).
 */
public class Pago {

    private int idPago;
    private int idProyecto;
    private String concepto;
    private BigDecimal monto = BigDecimal.ZERO;
    private LocalDate fechaPago;
    private String estadoCuenta = "Pagado";
    private String metodoPago = "Transferencia";
    private boolean activo = true;

    // Campos descriptivos obtenidos mediante JOIN
    private String nombreProyecto;
    private String nombreCliente;
    private String rfcCliente;

    public Pago() {
    }

    public Pago(int idPago, int idProyecto, String concepto, BigDecimal monto, LocalDate fechaPago,
            String estadoCuenta, String metodoPago, boolean activo,
            String nombreProyecto, String nombreCliente, String rfcCliente) {
        this.idPago = idPago;
        this.idProyecto = idProyecto;
        this.concepto = concepto;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.fechaPago = fechaPago;
        this.estadoCuenta = estadoCuenta;
        this.metodoPago = metodoPago;
        this.activo = activo;
        this.nombreProyecto = nombreProyecto;
        this.nombreCliente = nombreCliente;
        this.rfcCliente = rfcCliente;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getMonto() {
        return monto != null ? monto : BigDecimal.ZERO;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto != null ? monto : BigDecimal.ZERO;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getActivoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getRfcCliente() {
        return rfcCliente;
    }

    public void setRfcCliente(String rfcCliente) {
        this.rfcCliente = rfcCliente;
    }

    public String getFolio() {
        return String.format("PAG-%04d", idPago);
    }

    public String getProyectoDescriptivo() {
        if (nombreProyecto == null || nombreProyecto.isBlank()) {
            return "PRY-" + String.format("%04d", idProyecto);
        }
        return "PRY-" + String.format("%04d", idProyecto) + " · " + nombreProyecto;
    }

    @Override
    public String toString() {
        return getFolio() + " - " + (concepto != null ? concepto : getProyectoDescriptivo());
    }
}
