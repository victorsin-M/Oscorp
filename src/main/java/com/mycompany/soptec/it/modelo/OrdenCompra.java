package com.mycompany.soptec.it.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de la tabla orden_compra (Proyecto 1:N OrdenCompra, Proveedor 1:N opcional, M:N con Material).
 */
public class OrdenCompra {

    private int idOrdenCompra;
    private int idProyecto;
    private Integer idProveedor;
    private LocalDate fechaOrden;
    private BigDecimal totalCompra = BigDecimal.ZERO;
    private String tiempoEntrega;
    private String estadoOrden = "Solicitada";
    private boolean activo = true;

    // Campos descriptivos obtenidos mediante JOIN
    private String nombreProyecto;
    private String nombreProveedor;
    private int totalPartidas;

    // Partidas asociadas (orden_compra_material)
    private List<OrdenCompraMaterial> partidas = new ArrayList<>();

    public OrdenCompra() {
    }

    public OrdenCompra(int idOrdenCompra, int idProyecto, Integer idProveedor, LocalDate fechaOrden,
            BigDecimal totalCompra, String tiempoEntrega, String estadoOrden, boolean activo,
            String nombreProyecto, String nombreProveedor, int totalPartidas) {
        this.idOrdenCompra = idOrdenCompra;
        this.idProyecto = idProyecto;
        this.idProveedor = idProveedor;
        this.fechaOrden = fechaOrden;
        this.totalCompra = totalCompra != null ? totalCompra : BigDecimal.ZERO;
        this.tiempoEntrega = tiempoEntrega;
        this.estadoOrden = estadoOrden != null ? estadoOrden : "Solicitada";
        this.activo = activo;
        this.nombreProyecto = nombreProyecto;
        this.nombreProveedor = nombreProveedor;
        this.totalPartidas = totalPartidas;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public LocalDate getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(LocalDate fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra != null ? totalCompra : BigDecimal.ZERO;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra != null ? totalCompra : BigDecimal.ZERO;
    }

    public String getTiempoEntrega() {
        return tiempoEntrega;
    }

    public void setTiempoEntrega(String tiempoEntrega) {
        this.tiempoEntrega = tiempoEntrega;
    }

    public String getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(String estadoOrden) {
        this.estadoOrden = estadoOrden;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getActivoTexto() {
        return activo ? "Activa" : "Inactiva";
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public int getTotalPartidas() {
        return totalPartidas;
    }

    public void setTotalPartidas(int totalPartidas) {
        this.totalPartidas = totalPartidas;
    }

    public List<OrdenCompraMaterial> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<OrdenCompraMaterial> partidas) {
        this.partidas = partidas != null ? new ArrayList<>(partidas) : new ArrayList<>();
    }

    public String getFolio() {
        return String.format("OC-%04d", idOrdenCompra);
    }

    public String getProyectoDescriptivo() {
        if (nombreProyecto == null || nombreProyecto.isBlank()) {
            return "PRY-" + String.format("%04d", idProyecto);
        }
        return "PRY-" + String.format("%04d", idProyecto) + " · " + nombreProyecto;
    }

    public String getProveedorDescriptivo() {
        return (nombreProveedor == null || nombreProveedor.isBlank()) ? "Sin proveedor asignado" : nombreProveedor;
    }

    @Override
    public String toString() {
        return getFolio() + " · " + getProyectoDescriptivo();
    }
}
