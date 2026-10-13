
package com.mycompany.soptec.it.modelo;

import java.util.ArrayList;
import java.util.List;
public class Pedido {
    private int id;
    private int clienteId;
    private double iva;
    private String fecha;
    private double descuento;
    private double total;
    private String estado;
    private List<ProductosPedido> productos;

    public Pedido(int id, int clienteId, double iva, String fecha, double descuento, double total, String estado, List<ProductosPedido> productos) {
        this.id = id;
        this.clienteId = clienteId;
        this.iva = iva;
        this.fecha = fecha;
        this.descuento = descuento;
        this.total = total;
        this.estado = estado;
        this.productos = productos;
    }
    
    public double calcularIva(double subtotal){
        this.iva = subtotal * 0.16;
        return iva;
    }
    
    public double calcularTotal(double subtotal){
        double totalConIva = subtotal + calcularIva(subtotal);
        this.total = totalConIva - (totalConIva * descuento / 100);
        return total;
    }
    
    public void aplicarDescuento(double porcentaje){
    this.descuento = porcentaje;
    }
    
    public void cambiarEstado(String nuevoEstado){
        this.estado = nuevoEstado;
    }
    
    public boolean cancelarPedido(){
    if(estado.equals("Entregado")){
            return false;
        }
        this.estado = "Cancelado";
        return true;
    }
    
    public String generarComprobante(){
        return "Pedido #"+id+" - Estado: "+ estado + " - Total: $"+total;
        
    }
    public void agregarProducto(ProductosPedido producto){
        productos.add(producto);
    }
    public List<ProductosPedido> getProductos(){
        return productos;
    }
}
