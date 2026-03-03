package com.aulaclick.app.network.models;

import java.util.List;

public class Recurso {
    private Integer idRecurso;
    private String nombre;
    private String tipo;
    private Integer capacidad;
    private String estado;
    private Integer idDepartamento;
    private List<Integer> idsEquipamiento;

    public Recurso() {
    }

    public Recurso(String nombre, String tipo, Integer capacidad, String estado, Integer idDepartamento, List<Integer> idsEquipamiento) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.estado = estado;
        this.idDepartamento = idDepartamento;
        this.idsEquipamiento = idsEquipamiento;
    }

    public Integer getIdRecurso() {
        return idRecurso;
    }

    public void setIdRecurso(Integer idRecurso) {
        this.idRecurso = idRecurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Integer idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public List<Integer> getIdsEquipamiento() {
        return idsEquipamiento;
    }

    public void setIdsEquipamiento(List<Integer> idsEquipamiento) {
        this.idsEquipamiento = idsEquipamiento;
    }
}
