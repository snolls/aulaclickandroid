package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioResponse {
    @SerializedName("idUsuario")
    private Integer idUsuario;
    
    @SerializedName("nombreCompleto")
    private String nombreCompleto;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("rol")
    private String rol;

    public Integer getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
}
