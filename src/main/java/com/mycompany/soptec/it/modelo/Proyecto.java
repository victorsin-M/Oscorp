package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de la entidad base proyecto (tabla proyecto).
 */
public class Proyecto {

    private int idProyecto;
    private String nombreProyecto;
    private LocalDate fechaInicio;
    private LocalDate fechaEstimadaFin;
    private String estado;
    private BigDecimal costoTotal = BigDecimal.ZERO;
    private int idCliente;
    private boolean activo = true;

    // Campos descriptivos obtenidos mediante JOIN con cliente
    private String clienteNombre;
    private String clienteRfc;
    private Cliente cliente;
    private List<Material> materiales = new ArrayList<>();

    public Proyecto() {
    }

    public Proyecto(int idProyecto, Cliente cliente, String estatus, String fechainicio,
            double costoTotal, List<Material> materiales) {
        this.idProyecto = idProyecto;
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getIdCliente();
            this.clienteNombre = cliente.getNombreCliente();
            this.clienteRfc = cliente.getRfc();
        }
        this.estado = estatus;
        if (fechainicio != null && !fechainicio.isBlank()) {
            this.fechaInicio = LocalDate.parse(fechainicio);
        }
        this.costoTotal = BigDecimal.valueOf(costoTotal);
        this.materiales = materiales != null ? materiales : new ArrayList<>();
    }

    public Proyecto(int idProyecto, String nombreProyecto, LocalDate fechaInicio, LocalDate fechaEstimadaFin,
            String estado, BigDecimal costoTotal, int idCliente, boolean activo,
            String clienteNombre, String clienteRfc) {
        this.idProyecto = idProyecto;
        this.nombreProyecto = nombreProyecto;
        this.fechaInicio = fechaInicio;
        this.fechaEstimadaFin = fechaEstimadaFin;
        this.estado = estado;
        this.costoTotal = costoTotal != null ? costoTotal : BigDecimal.ZERO;
        this.idCliente = idCliente;
        this.activo = activo;
        this.clienteNombre = clienteNombre;
        this.clienteRfc = clienteRfc;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaEstimadaFin() {
        return fechaEstimadaFin;
    }

    public void setFechaEstimadaFin(LocalDate fechaEstimadaFin) {
        this.fechaEstimadaFin = fechaEstimadaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal != null ? costoTotal : BigDecimal.ZERO;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
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

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteRfc() {
        return clienteRfc;
    }

    public void setClienteRfc(String clienteRfc) {
        this.clienteRfc = clienteRfc;
    }

    public String getClienteDescriptivo() {
        if (clienteNombre == null || clienteNombre.isBlank()) {
            return "Sin cliente";
        }
        return (clienteRfc != null && !clienteRfc.isBlank())
                ? clienteNombre + " (" + clienteRfc + ")"
                : clienteNombre;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void asociarCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getIdCliente();
            this.clienteNombre = cliente.getNombreCliente();
            this.clienteRfc = cliente.getRfc();
        }
    }

    public void cambiarEstatus(String nuevosEstatus) {
        this.estado = nuevosEstatus;
    }

    public double calcularCostoTotal(double costosAdicionales) {
        this.costoTotal = BigDecimal.valueOf(costosAdicionales);
        return this.costoTotal.doubleValue();
    }

    public String obtenerResumen() {
        return nombreProyecto + " · Cliente: " + getClienteDescriptivo() + " · Estado: " + estado;
    }

    public List<Material> getMateriales() {
        return materiales;
    }

    @Override
    public String toString() {
        String cli = clienteNombre != null && !clienteNombre.isBlank() ? " — " + clienteNombre : "";
        String est = estado != null && !estado.isBlank() ? " [" + estado + "]" : "";
        return nombreProyecto + cli + est + (activo ? "" : " (Inactivo)");
    }
}
