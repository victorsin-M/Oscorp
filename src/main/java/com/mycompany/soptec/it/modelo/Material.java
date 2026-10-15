
package com.mycompany.soptec.it.modelo;
public class Material {
    private  int idMaterial;
    private String codigo;
    private String nombre;
    private double precioUnitario;
    private int stockActual;

    public Material(int idMaterial, String codigo, String nombre, double precioUnitario, int stockActual) {
        this.idMaterial = idMaterial;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
    }
    
    public void actualizarPrecio(double nuevoPrecio){
        this.precioUnitario = nuevoPrecio;
    }
    public double calcularSubtotal(int cantidad){
        return cantidad * precioUnitario;
    }
    public String obtenerDetalles(){
        return nombre + " ("+ codigo + ") - $"+ precioUnitario;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getStockActual() {
        return stockActual;
    }
    
    
}
