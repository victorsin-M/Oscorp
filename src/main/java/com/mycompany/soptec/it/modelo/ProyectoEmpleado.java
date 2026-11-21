package com.mycompany.soptec.it.modelo;

/**
 * Modelo de la tabla puente proyecto_empleado (relacion M:N Proyecto <-> Empleado).
 */
public class ProyectoEmpleado {

    private int idProyecto;
    private int idEmpleado;

    // Campos descriptivos obtenidos mediante JOIN
    private String nombreProyecto;
    private String estadoProyecto;
    private String nombreCliente;
    private String nombreEmpleado;
    private String puestoEmpleado;
    private String correoEmpleado;

    public ProyectoEmpleado() {
    }

    public ProyectoEmpleado(int idProyecto, int idEmpleado) {
        this.idProyecto = idProyecto;
        this.idEmpleado = idEmpleado;
    }

    public ProyectoEmpleado(int idProyecto, int idEmpleado, String nombreProyecto, String estadoProyecto,
            String nombreCliente, String nombreEmpleado, String puestoEmpleado, String correoEmpleado) {
        this.idProyecto = idProyecto;
        this.idEmpleado = idEmpleado;
        this.nombreProyecto = nombreProyecto;
        this.estadoProyecto = estadoProyecto;
        this.nombreCliente = nombreCliente;
        this.nombreEmpleado = nombreEmpleado;
        this.puestoEmpleado = puestoEmpleado;
        this.correoEmpleado = correoEmpleado;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getEstadoProyecto() {
        return estadoProyecto;
    }

    public void setEstadoProyecto(String estadoProyecto) {
        this.estadoProyecto = estadoProyecto;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getPuestoEmpleado() {
        return puestoEmpleado;
    }

    public void setPuestoEmpleado(String puestoEmpleado) {
        this.puestoEmpleado = puestoEmpleado;
    }

    public String getCorreoEmpleado() {
        return correoEmpleado;
    }

    public void setCorreoEmpleado(String correoEmpleado) {
        this.correoEmpleado = correoEmpleado;
    }

    public String getProyectoDescriptivo() {
        if (nombreProyecto == null || nombreProyecto.isBlank()) {
            return "PRY-" + String.format("%04d", idProyecto);
        }
        return "PRY-" + String.format("%04d", idProyecto) + " · " + nombreProyecto;
    }

    public String getEmpleadoDescriptivo() {
        if (nombreEmpleado == null || nombreEmpleado.isBlank()) {
            return "Empleado #" + idEmpleado;
        }
        return nombreEmpleado + (puestoEmpleado != null && !puestoEmpleado.isBlank() ? " (" + puestoEmpleado + ")" : "");
    }

    @Override
    public String toString() {
        return getProyectoDescriptivo() + " <-> " + getEmpleadoDescriptivo();
    }
}
