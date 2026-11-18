package com.mycompany.soptec.it.modelo;

import java.util.List;

/**
 * Modelo de la entidad base cliente (tabla cliente).
 */
public class Cliente extends Persona {

    private int idCliente;
    private String rfc;
    private String nombreCliente;
    private String calle;
    private String numero;
    private String colonia;
    private String estado;
    private String imagen;
    private boolean activo = true;
    private List<Proyecto> historialProyecto;

    public Cliente() {
        super(0, "", "", "");
    }

    public Cliente(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
        this.idCliente = idPersona;
        this.nombreCliente = nombre;
    }

    public Cliente(int idCliente, String rfc, String nombreCliente, String correo,
            String calle, String numero, String colonia, String estado, String imagen, boolean activo) {
        super(idCliente, nombreCliente, "", correo);
        this.idCliente = idCliente;
        this.rfc = rfc;
        this.nombreCliente = nombreCliente;
        this.calle = calle;
        this.numero = numero;
        this.colonia = colonia;
        this.estado = estado;
        this.imagen = imagen;
        this.activo = activo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
        setIdPersona(idCliente);
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
        setNombre(nombreCliente);
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getDireccionResumen() {
        StringBuilder sb = new StringBuilder();
        if (calle != null && !calle.isBlank()) {
            sb.append(calle);
        }
        if (numero != null && !numero.isBlank()) {
            sb.append(sb.length() > 0 ? " #" : "#").append(numero);
        }
        if (colonia != null && !colonia.isBlank()) {
            sb.append(sb.length() > 0 ? ", " : "").append(colonia);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(sb.length() > 0 ? ", " : "").append(estado);
        }
        return sb.toString();
    }

    public List<Proyecto> getHistorialProyecto() {
        return historialProyecto;
    }

    public void setHistorialProyecto(List<Proyecto> historialProyecto) {
        this.historialProyecto = historialProyecto;
    }

    @Override
    public String toString() {
        String nom = nombreCliente != null ? nombreCliente : getNombre();
        String rfcTxt = (rfc != null && !rfc.isBlank()) ? " (" + rfc + ")" : "";
        String estadoTxt = activo ? "" : " [Inactivo]";
        return nom + rfcTxt + estadoTxt;
    }
}
