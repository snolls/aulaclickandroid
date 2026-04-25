package com.aulaclick.app;

public class Recurso {
    private String nombre;
    private String capacidad;
    private String estado; // "Libre", "Ocupado", "Mantenimiento"

    public Recurso(String nombre, String capacidad, String estado) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public String getNombre() { return nombre; }
    public String getCapacidad() { return capacidad; }
    public String getEstado() { return estado; }
}
