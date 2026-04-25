package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class Departamento {
    @SerializedName("idDepartamento")
    private Integer id;
    private String nombre;
    @SerializedName("sedeId")
    private Long sedeId;

    public Departamento() {}

    public Departamento(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    @Override
    public String toString() {
        return nombre;
    }
}
