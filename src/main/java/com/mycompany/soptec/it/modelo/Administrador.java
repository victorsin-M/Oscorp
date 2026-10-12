
package com.mycompany.soptec.it.modelo;
public class Administrador extends Empleado{
    private String usuario;
    private String contrasena;
    private int nivel;

    public Administrador(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nivel = nivel;
    }
    
    public boolean autenticar(String usuario , String contrasena){
        return this.usuario.equals(usuario) && this.contrasena.equals(contrasena);
    }
    public void asiganarNivel(int nuevoNivel){
        this.nivel = nuevoNivel;
    }
}
