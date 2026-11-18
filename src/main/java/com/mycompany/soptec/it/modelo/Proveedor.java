package com.mycompany.soptec.it.modelo;

/**
 * Modelo de la entidad base proveedor (tabla proveedor).
 */
public class Proveedor {

    private int idProveedor;
    private String nombreProveedor;
    private String telefonoProveedor;
    private String correoProveedor;
    private String imagen;
    private boolean activo = true;

    public Proveedor() {
    }

    public Proveedor(int idProveedor, String nombreProveedor, String telefonoProveedor,
            String correoProveedor, String imagen, boolean activo) {
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
        this.telefonoProveedor = telefonoProveedor;
        this.correoProveedor = correoProveedor;
        this.imagen = imagen;
        this.activo = activo;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getTelefonoProveedor() {
        return telefonoProveedor;
    }

    public void setTelefonoProveedor(String telefonoProveedor) {
        this.telefonoProveedor = telefonoProveedor;
    }

    public String getCorreoProveedor() {
        return correoProveedor;
    }

    public void setCorreoProveedor(String correoProveedor) {
        this.correoProveedor = correoProveedor;
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

    @Override
    public String toString() {
        return nombreProveedor + (activo ? "" : " [Inactivo]");
    }
}
