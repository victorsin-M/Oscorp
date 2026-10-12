
package com.mycompany.soptec.it.modelo;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Material;

public class Proyecto {
    private int idProyecto;
    private Cliente cliente;
    private String estatus;
    private String fechainicio;
    private double costoTotal;
    private List<Material> materiales;

    public Proyecto(int idProyecto, Cliente cliente, String estatus, String fechainicio, double costoTotal, List<Material> materiales) {
        this.idProyecto = idProyecto;
        this.cliente = cliente;
        this.estatus = estatus;
        this.fechainicio = fechainicio;
        this.costoTotal = costoTotal;
        this.materiales = new ArrayList<>();
    }
    
    public void cambiarEstatus(String nuevosEstatus){
        this.estatus = nuevosEstatus;
    }
    
    public double calcularCostoTotal(double costosAdicionales){
        this.costoTotal = costosAdicionales;
        return this.costoTotal;
    }
    public String obtenerResumen() {
        return "Proyecto{" + "idProyecto=" + idProyecto + ", cliente=" + cliente + ", estatus=" + estatus + ", fechainicio=" + fechainicio + ", costoTotal=" + costoTotal + ", materiales=" + materiales + '}';
    }
    public void asociarCliente(Cliente cliente){
        this.cliente = cliente;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getEstatus() {
        return estatus;
    }

    public String getFechainicio() {
        return fechainicio;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public List<Material> getMateriales() {
        return materiales;
    }
    
}
