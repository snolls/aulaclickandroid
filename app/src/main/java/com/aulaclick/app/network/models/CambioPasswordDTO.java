package com.aulaclick.app.network.models;

public class CambioPasswordDTO {
    private String passwordActual;
    private String nuevaPassword;

    public CambioPasswordDTO(String passwordActual, String nuevaPassword) {
        this.passwordActual = passwordActual;
        this.nuevaPassword = nuevaPassword;
    }

    public String getPasswordActual() { return passwordActual; }
    public String getNuevaPassword() { return nuevaPassword; }
}
