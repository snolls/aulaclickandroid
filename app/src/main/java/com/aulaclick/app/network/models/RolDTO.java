package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class RolDTO {
    @SerializedName(value = "idRol", alternate = {"id", "id_rol", "rolId"})
    private Long id;
    @SerializedName(value = "nombre", alternate = {"nombreRol", "name"})
    private String nombre;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre != null ? nombre : "—"; }
}
