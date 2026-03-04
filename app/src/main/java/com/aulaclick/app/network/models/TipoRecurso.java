package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class TipoRecurso {
    @SerializedName("idTipoRecurso")
    private Integer id;
    private String nombre;

    public TipoRecurso() {}

    public TipoRecurso(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}
