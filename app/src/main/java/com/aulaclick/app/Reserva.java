package com.aulaclick.app;

/**
 * Modelo de datos en Android para representar una Reserva.
 * Se elige mantener campos en formato String (fecha, hora) por simplicidad
 * al parsear la respuesta JSON del servidor, delegando la lógica de formato
 * al adaptador de UI.
 */
public class Reserva {
    private Long id;
    private String nombreRecurso;
    private String fecha;
    private String hora;
    private String estado;
    private String imagenUrl;

    public Reserva(Long id, String nombreRecurso, String fecha, String hora, String estado, String imagenUrl) {
        this.id = id;
        this.nombreRecurso = nombreRecurso;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.imagenUrl = imagenUrl;
    }

    private String nombreUsuario;

    public Long getIdReserva() { return id; }
    public String getNombreRecurso() { return nombreRecurso; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getImagenUrl() { return imagenUrl; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
