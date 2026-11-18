package com.mycompany.soptec.it.modelo;

/**
 * Modelo de la entidad base cliente_telefono.
 */
public class ClienteTelefono {

    private int idCliente;
    private String telefono;
    private boolean activo = true;
    private String nombreCliente;
    private String rfcCliente;

    public ClienteTelefono() {
    }

    public ClienteTelefono(int idCliente, String telefono, boolean activo) {
        this.idCliente = idCliente;
        this.telefono = telefono;
        this.activo = activo;
    }

    public ClienteTelefono(int idCliente, String telefono, boolean activo, String nombreCliente, String rfcCliente) {
        this.idCliente = idCliente;
        this.telefono = telefono;
        this.activo = activo;
        this.nombreCliente = nombreCliente;
        this.rfcCliente = rfcCliente;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
            return "Cliente sin nombre";
        }
        return (rfcCliente != null && !rfcCliente.isBlank())
                ? nombreCliente + " (" + rfcCliente + ")"
                : nombreCliente;
    }

    @Override
    public String toString() {
        return telefono + " - " + getClienteDescriptivo();
    }
}
