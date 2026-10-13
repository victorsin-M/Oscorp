
package com.mycompany.soptec.it.modelo;

public class ProductosPedido {
    private int idProductoPedido;
    private int idPedido;
    private int idProducto;
    private int cantidad;
    private double precio;
    private double descuento;

    public ProductosPedido(int idProductoPedido, int idPedido, int idProducto, int cantidad, double precio, double descuento) {
        this.idProductoPedido = idProductoPedido;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.descuento = descuento;
    }
    
     public double calcularSunTotal(){
         double subtotal = cantidad * precio;
         return subtotal - (subtotal * descuento / 100);
         
     }
     public void aplicarDescuento(double porcentaje){
         this.descuento = porcentaje;
     }
     
     public void actualizarCantidad(int nuevaCantidad){
         this.cantidad = nuevaCantidad;
     }
     
     public String obtenerDetalleItem(){
     return "Producto #"+ idProducto + "x"+ cantidad + " = "+ calcularSunTotal();
     }

    

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }
}
