
package com.mycompany.soptec.it.modelo;
public class CableEstructurado extends Material{
    
    private String categoria;
    private float longitudMetros;
    
    
    public CableEstructurado(int idMaterial, String codigo, String nombre, double precioUnitario, int stockActual) {
        super(idMaterial, codigo, nombre, precioUnitario, stockActual);
        this.categoria = categoria;
        this.longitudMetros = longitudMetros;
    }
    public boolean verificarCategoria(String categoriaRequirida){
        return this.categoria.equalsIgnoreCase(categoriaRequirida);
    }
    
    public int calcularBobinasNecesarias(float metrosTotales){
        float metroPorBobina = 305F;
        return (int) Math.ceil(metrosTotales / metroPorBobina);
    }
    
    @Override
    public String obtenerDetalles(){
        return super.obtenerDetalles() + " - Cable"+ categoria + ", "+ longitudMetros+ "m";
    }
}
