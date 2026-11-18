package com.mycompany.soptec.it.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modelo de la entidad base cita (tabla cita).
 */
public class Cita {

    private int idCita;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String estadoCita;
    private String observaciones;
    private int idCliente;
    private boolean activo = true;

    // Datos descriptivos del cliente asociado
    private String nombreCliente;
    private String rfcCliente;

    public Cita() {
    }

    public Cita(int idCita, LocalDate fechaCita, LocalTime horaCita, String estadoCita,
            String observaciones, int idCliente, boolean activo, String nombreCliente, String rfcCliente) {
        this.idCita = idCita;
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.estadoCita = estadoCita;
        this.observaciones = observaciones;
        this.idCliente = idCliente;
        this.activo = activo;
        this.nombreCliente = nombreCliente;
        this.rfcCliente = rfcCliente;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDate fechaCita) {
        this.fechaCita = fechaCita;
    }

    public LocalTime getHoraCita() {
        return horaCita;
    }

    public void setHoraCita(LocalTime horaCita) {
        this.horaCita = horaCita;
    }

    public String getEstadoCita() {
        return estadoCita;
    }

    public void setEstadoCita(String estadoCita) {
        this.estadoCita = estadoCita;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public String getClienteDescriptivo() {
        if (nombreCliente == null || nombreCliente.isBlank()) {
            return "Sin cliente";
        }
        return (rfcCliente != null && !rfcCliente.isBlank())
                ? nombreCliente + " (" + rfcCliente + ")"
                : nombreCliente;
    }

    @Override
    public String toString() {
        String f = fechaCita != null ? fechaCita.toString() : "";
        String h = horaCita != null ? horaCita.toString() : "";
        String c = nombreCliente != null ? nombreCliente : "";
        return f + " " + h + " — " + c + " (" + estadoCita + ")";
    }
}
