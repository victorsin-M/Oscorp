
package com.mycompany.soptec.it.modelo;
public class AgenteVentas extends Empleado{
    private  double comision;
    private double metaVentas;

    public AgenteVentas(int idPersona, String nombre, String telefono, String correo) {
        super(idPersona, nombre, telefono, correo);
        this.comision = comision;
        this.metaVentas = metaVentas;
    }

   public void registrarComision(){
       
   }
    public boolean evaularMetaVentas(){
        return false;
    }
    
    public void registrarCotizacion(Cliente cliente , Proyecto proyecto){
        
    }
}
