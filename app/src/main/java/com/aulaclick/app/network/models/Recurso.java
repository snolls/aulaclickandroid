package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Recurso {
    @SerializedName("idRecurso")
    private Integer id;
    private String nombre;
    private Integer idTipoRecurso;
    private Integer capacidad;
    private String estado;
    private Integer idDepartamento;
    private List<Integer> idsEquipamiento;
    
    // Campo auxiliar para mostrar el nombre del tipo en la lista si fuera necesario
    private String nombreTipo;

    public Recurso() {
    }

    public Recurso(String nombre, Integer idTipoRecurso, Integer capacidad, String estado, Integer idDepartamento, List<Integer> idsEquipamiento) {
        this.nombre = nombre;
        this.idTipoRecurso = idTipoRecurso;
        this.capacidad = capacidad;
        this.estado = estado;
        this.idDepartamento = idDepartamento;
        this.idsEquipamiento = idsEquipamiento;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdTipoRecurso() {
        return idTipoRecurso;
    }

    public void setIdTipoRecurso(Integer idTipoRecurso) {
        this.idTipoRecurso = idTipoRecurso;
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

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }
}
