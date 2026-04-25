package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioRequestDTO {
    @SerializedName("nombreCompleto")
    private String nombreCompleto;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("idRol")
    private Long idRol;
    @SerializedName("idSede")
    private Long idSede;

    public UsuarioRequestDTO() {}

    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setEmail(String email)                   { this.email = email; }
    public void setPassword(String password)             { this.password = password; }
    public void setIdRol(Long idRol)                     { this.idRol = idRol; }
    public void setIdSede(Long idSede)                   { this.idSede = idSede; }

    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail()          { return email; }
    public String getPassword()       { return password; }
    public Long getIdRol()            { return idRol; }
    public Long getIdSede()           { return idSede; }
}
