package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioDTO {
    @SerializedName("id")
    private Long id;
    @SerializedName(value = "nombreCompleto", alternate = {"nombre", "name"})
    private String nombreCompleto;
    @SerializedName("email")
    private String email;
    @SerializedName("rol")
    private String rol;
    @SerializedName(value = "idRol", alternate = {"rol_id", "rolId"})
    private Long idRol;
    @SerializedName(value = "sedeId", alternate = {"idSede", "sede_id"})
    private Long sedeId;
    @SerializedName("sedeNombre")
    private String sedeNombre;

    public Long getId()              { return id; }
    public String getNombreCompleto(){ return nombreCompleto; }
    public String getEmail()         { return email; }
    public String getRol()           { return rol; }
    public Long getIdRol()           { return idRol; }
    public Long getSedeId()          { return sedeId; }
    public String getSedeNombre()    { return sedeNombre; }
}
