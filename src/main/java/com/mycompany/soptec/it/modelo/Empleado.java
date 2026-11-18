package com.mycompany.soptec.it.modelo;

/**
 * Modelo de la entidad base empleado (tabla empleado).
 */
public class Empleado extends Persona {

    private int idEmpleado;
    private String puesto;
    private String departamento;
    private String imagen;
    private boolean activo = true;

    public Empleado() {
        super(0, "", "", "");
    }

    public Empleado(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
        this.idEmpleado = idPersona;
    }

    public Empleado(int idEmpleado, String nombre, String telefono, String puesto,
            String correo, String imagen, boolean activo) {
        super(idEmpleado, nombre, telefono, correo);
        this.idEmpleado = idEmpleado;
        this.puesto = puesto;
        this.imagen = imagen;
        this.activo = activo;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
        setIdPersona(idEmpleado);
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
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

    public String consultarPuesto() {
        return puesto;
    }

    public void cambiarDepartamento(String nuevoDepto) {
        this.departamento = nuevoDepto;
    }

    public void registrarAsistencia() {
    }

    @Override
    public String toString() {
        return getNombre() + (puesto != null && !puesto.isBlank() ? " - " + puesto : "");
    }
}
