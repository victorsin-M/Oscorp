
package com.mycompany.soptec.it.modelo;
public class Empleado extends Persona {
    private int numEmpleado;
    private String puesto;
    private String departamento;

    public Empleado(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
        this.numEmpleado = numEmpleado;
        this.departamento = departamento;
        this.puesto = puesto;
    }
    
    public String consultarPuesto(){
        return puesto;
    }
    public void cambiarDepartamento(String nuevoDepto){
        this.departamento = nuevoDepto;
    }
    public void registrarAsistencia(){
        
    }
}
