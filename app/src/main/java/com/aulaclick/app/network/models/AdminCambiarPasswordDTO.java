package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class AdminCambiarPasswordDTO {
    @SerializedName("nuevaPassword")
    private final String nuevaPassword;

    public AdminCambiarPasswordDTO(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public String getNuevaPassword() { return nuevaPassword; }
}
