package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class TipoRecurso {
    @SerializedName("idTipoRecurso")
    private Integer id;
    private String nombre;
    @SerializedName("imagenUrl")
    private String imagenUrl;
    @SerializedName("sedeId")
    private Long sedeId;

    public TipoRecurso() {}

    public TipoRecurso(String nombre, String imagenUrl) {
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    @Override
    public String toString() {
        return nombre;
    }
}
