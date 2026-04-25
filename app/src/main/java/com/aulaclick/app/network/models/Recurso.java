package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Recurso {
    @SerializedName("idRecurso")
    private Integer id;
    private String nombre;
    private TipoRecurso tipoRecurso;
    private Integer capacidad;
    private String estado;
    private Departamento departamento;
    private List<Equipamiento> equipamientos;
    @SerializedName("imagenUrl")
    private String imagenUrl;
    @SerializedName(value = "idImagen", alternate = {"id_imagen", "imagen_id"})
    private Long idImagen;
    private Boolean permiteFinesSemana;
    private String horaApertura;
    private String horaCierre;
    @SerializedName("sedeId")
    private Long sedeId;
    @SerializedName("sedeNombre")
    private String sedeNombre;

    public Recurso() {
    }

    public Recurso(String nombre, TipoRecurso tipoRecurso, Integer capacidad, String estado, Departamento departamento, List<Equipamiento> equipamientos) {
        this.nombre = nombre;
        this.tipoRecurso = tipoRecurso;
        this.capacidad = capacidad;
        this.estado = estado;
        this.departamento = departamento;
        this.equipamientos = equipamientos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public void setTipoRecurso(TipoRecurso tipoRecurso) {
        this.tipoRecurso = tipoRecurso;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public List<Equipamiento> getEquipamientos() {
        return equipamientos;
    }

    public void setEquipamientos(List<Equipamiento> equipamientos) {
        this.equipamientos = equipamientos;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Long getIdImagen() { return idImagen; }
    public void setIdImagen(Long idImagen) { this.idImagen = idImagen; }

    public Boolean getPermiteFinesSemana() { return permiteFinesSemana; }
    public void setPermiteFinesSemana(Boolean permiteFinesSemana) { this.permiteFinesSemana = permiteFinesSemana; }
    
    public String getHoraApertura() { return horaApertura; }
    public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }
    
    public String getHoraCierre() { return horaCierre; }
    public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }

    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }
}
