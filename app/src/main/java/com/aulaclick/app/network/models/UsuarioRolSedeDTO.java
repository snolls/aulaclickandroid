package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioRolSedeDTO {
    @SerializedName("idRol")
    private Long   idRol;
    @SerializedName("idSede")
    private Long   idSede;
    @SerializedName("nombreCompleto")
    private String nombreCompleto;
    @SerializedName("email")
    private String email;

    public UsuarioRolSedeDTO(Long idRol, Long idSede, String nombreCompleto, String email) {
        this.idRol          = idRol;
        this.idSede         = idSede;
        this.nombreCompleto = nombreCompleto;
        this.email          = email;
    }
}
