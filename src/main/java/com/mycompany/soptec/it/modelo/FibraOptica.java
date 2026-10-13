
package com.mycompany.soptec.it.modelo;
public class FibraOptica extends Material{

    private String tipoFibra;
    private float longitudMetros;

    public FibraOptica(int idMaterial, String codigo, String nombre, double precioUnitario, int stockActual) {
        super(idMaterial, codigo, nombre, precioUnitario, stockActual);
        this.tipoFibra = tipoFibra;
        this.longitudMetros = longitudMetros;
    }
    
    public boolean verificarCompatibilidad(String tipoTransceptor){
    return this.tipoFibra.equalsIgnoreCase(tipoTransceptor);
    }
    public float calcularMetrajeNecesario(float distanciaMetros){
        float margenMerma = distanciaMetros * 0.10F;
        return distanciaMetros = margenMerma;
    }

    @Override
    public String obtenerDetalles(){
        return super.obtenerDetalles()+"- Fibra"+tipoFibra +", "+ longitudMetros + "m";
    }
}
