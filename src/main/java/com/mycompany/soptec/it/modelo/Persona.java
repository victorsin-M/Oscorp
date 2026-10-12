
package com.mycompany.soptec.it.modelo;
public class Persona {
    private int idPersona;
    private String nombre;
    private String telefono;
    private String correo;

    public Persona(int idPersona, String nombre, String telefono, String correo) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    public String getDatosContacto(){
        return nombre + " - "+ telefono + " - "+ correo;
    }
    
    public void actualizarTelefono(String nuevoTelefono){
        this.telefono = nuevoTelefono;
    }
    public void actualizarCorreo(String nuevoCorreo){
        this.correo = nuevoCorreo;
    }
    
    
}
