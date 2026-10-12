
package com.mycompany.soptec.it.modelo;

import java.util.List;

public class Cliente extends Persona{
   
    private String rfc;
    private String razonSocial;
    private List<Proyecto> historialProyecto;

    public Cliente(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
    }
    
    
}
