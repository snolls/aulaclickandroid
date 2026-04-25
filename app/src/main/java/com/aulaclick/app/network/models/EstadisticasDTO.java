package com.aulaclick.app.network.models;

public class EstadisticasDTO {
    private int totalUsuarios;
    private int totalRecursos;
    private int totalReservas;
    private int reservasEnCurso;
    private int totalReservasActivas;
    private int totalReservasCanceladas;

    public int getTotalUsuarios() { return totalUsuarios; }
    public int getTotalRecursos() { return totalRecursos; }
    public int getTotalReservas() { return totalReservas; }
    public int getReservasEnCurso() { return reservasEnCurso; }
    public int getTotalReservasActivas() { return totalReservasActivas; }
    public int getTotalReservasCanceladas() { return totalReservasCanceladas; }
}
