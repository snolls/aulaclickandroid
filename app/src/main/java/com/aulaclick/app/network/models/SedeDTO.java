package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class SedeDTO {
    @SerializedName(value = "idSede", alternate = {"id", "id_sede", "sedeId"})
    private Long id;
    
    @SerializedName(value = "nombre", alternate = {"nombreSede", "name"})
    private String nombre;

    @SerializedName("ciudad")
    private String ciudad;

    @SerializedName("direccion")
    private String direccion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() { 
        return nombre != null ? nombre : "—"; 
    }
}
