package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioRolSedeDTO {
    @SerializedName("idRol")
    private Long idRol;
    @SerializedName("idSede")
    private Long idSede;

    public UsuarioRolSedeDTO(Long idRol, Long idSede) {
        this.idRol  = idRol;
        this.idSede = idSede;
    }
}
