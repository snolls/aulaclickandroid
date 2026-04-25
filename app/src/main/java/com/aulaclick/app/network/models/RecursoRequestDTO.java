package com.aulaclick.app.network.models;

import java.util.List;

public class RecursoRequestDTO {
    private String nombre;
    private Integer capacidad;
    private String estado;
    private Long idDepartamento;
    private Long idTipoRecurso;
    private List<Long> idsEquipamientos;
    private Long idImagen;
    private Boolean permiteFinesSemana;
    private String horaApertura;
    private String horaCierre;
    private Long idSede;

    public RecursoRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Long getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Long idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public Long getIdTipoRecurso() {
        return idTipoRecurso;
    }

    public void setIdTipoRecurso(Long idTipoRecurso) {
        this.idTipoRecurso = idTipoRecurso;
    }

    public List<Long> getIdsEquipamientos() {
        return idsEquipamientos;
    }

    public void setIdsEquipamientos(List<Long> idsEquipamientos) {
        this.idsEquipamientos = idsEquipamientos;
    }

    public Long getIdImagen() { return idImagen; }
    public void setIdImagen(Long idImagen) { this.idImagen = idImagen; }

    public Boolean getPermiteFinesSemana() { return permiteFinesSemana; }
    public void setPermiteFinesSemana(Boolean permiteFinesSemana) { this.permiteFinesSemana = permiteFinesSemana; }
    
    public String getHoraApertura() { return horaApertura; }
    public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }
    
    public String getHoraCierre() { return horaCierre; }
    public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }

    public Long getIdSede() { return idSede; }
    public void setIdSede(Long idSede) { this.idSede = idSede; }
}

