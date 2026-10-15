
package com.mycompany.soptec.it.modelo;
public class EquipoRed extends Material {
    private String marca;
    private String modelo;
    private int numPuertos;

    public EquipoRed(String marca, String modelo, int numPersonas, int idMaterial, String codigo, String nombre, double precioUnitario, int stockActual) {
        super(idMaterial, codigo, nombre, precioUnitario, stockActual);
        this.marca = marca;
        this.modelo = modelo;
        this.numPuertos = numPuertos;
    }
    
    public boolean verificarPuertosDisponibles(int puertosRequeridos){
        return this.numPuertos >= puertosRequeridos;
    }
    
    public String obtenerFichaTecnica(){
        return marca + " "+ modelo + " - "+ numPuertos + " puertos";
    }
    @Override
    public String obtenerDetalles(){
        return super.obtenerDetalles() + " - "+ obtenerFichaTecnica();
    }
}
