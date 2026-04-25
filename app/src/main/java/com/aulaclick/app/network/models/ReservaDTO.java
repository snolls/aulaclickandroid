package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class ReservaDTO {
    @SerializedName(value = "id", alternate = {"idReserva", "id_reserva"})
    private Long id;
    @SerializedName("fecha")
    private String fecha;
    @SerializedName("horaInicio")
    private String horaInicio;
    @SerializedName("horaFin")
    private String horaFin;
    @SerializedName("motivo")
    private String motivo;
    @SerializedName("nombreUsuario")
    private String nombreUsuario;
    @SerializedName("estado")
    private String estado;
    @SerializedName("nombreRecurso")
    private String nombreRecurso;
    @SerializedName(value = "imagenUrl", alternate = {"imagen_url", "url_imagen", "imagen"})
    private String imagenUrl;

    public ReservaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreRecurso() { return nombreRecurso; }
    public void setNombreRecurso(String nombreRecurso) { this.nombreRecurso = nombreRecurso; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
