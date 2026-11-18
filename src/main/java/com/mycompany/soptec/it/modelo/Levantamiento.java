package com.mycompany.soptec.it.modelo;

import java.time.LocalDate;

/**
 * Modelo de la entidad base levantamiento (tabla levantamiento).
 */
public class Levantamiento {

    private int idLevantamiento;
    private LocalDate fechaVisita;
    private String croquis;
    private String observacionesTecnicas;
    private int idProyecto;
    private Integer idCita;
    private boolean activo = true;

    // Campos descriptivos obtenidos mediante JOIN
    private String nombreProyecto;
    private String nombreCliente;
    private String fechaHoraCita;

    public Levantamiento() {
    }

    public Levantamiento(int idLevantamiento, LocalDate fechaVisita, String croquis, String observacionesTecnicas,
            int idProyecto, Integer idCita, boolean activo,
            String nombreProyecto, String nombreCliente, String fechaHoraCita) {
        this.idLevantamiento = idLevantamiento;
        this.fechaVisita = fechaVisita;
        this.croquis = croquis;
        this.observacionesTecnicas = observacionesTecnicas;
        this.idProyecto = idProyecto;
        this.idCita = idCita;
        this.activo = activo;
        this.nombreProyecto = nombreProyecto;
        this.nombreCliente = nombreCliente;
        this.fechaHoraCita = fechaHoraCita;
    }

    public int getIdLevantamiento() {
        return idLevantamiento;
    }

    public void setIdLevantamiento(int idLevantamiento) {
        this.idLevantamiento = idLevantamiento;
    }

    public LocalDate getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(LocalDate fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public String getCroquis() {
        return croquis;
    }

    public void setCroquis(String croquis) {
        this.croquis = croquis;
    }

    public String getObservacionesTecnicas() {
        return observacionesTecnicas;
    }

    public void setObservacionesTecnicas(String observacionesTecnicas) {
        this.observacionesTecnicas = observacionesTecnicas;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
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

    public String getFechaHoraCita() {
        return fechaHoraCita;
    }

    public void setFechaHoraCita(String fechaHoraCita) {
        this.fechaHoraCita = fechaHoraCita;
    }

    public String getProyectoDescriptivo() {
        if (nombreProyecto == null || nombreProyecto.isBlank()) {
            return "Sin proyecto";
        }
        return (nombreCliente != null && !nombreCliente.isBlank())
                ? nombreProyecto + " — " + nombreCliente
                : nombreProyecto;
    }

    @Override
    public String toString() {
        return "Levantamiento " + fechaVisita + " · " + getProyectoDescriptivo();
    }
}
