package com.aulaclick.app;

public class Reserva {
    private String nombreRecurso;
    private String fecha;
    private String hora;
    private String estado; // "Confirmada", "Pendiente", "Cancelada"

    public Reserva(String nombreRecurso, String fecha, String hora, String estado) {
        this.nombreRecurso = nombreRecurso;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public String getNombreRecurso() { return nombreRecurso; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
}
