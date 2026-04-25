package com.aulaclick.app.utils;

public class ImageMapper {
    private static final String[] URLS_EDUCATIVAS = {
        "https://images.unsplash.com/photo-1541339907198-e08756ebafe3?w=800", // Aula 1
        "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=800", // Lab
        "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=800", // Auditorio
        "https://images.unsplash.com/photo-1497366754035-f200968a6e72?w=800", // Sala reuniones
        "https://images.unsplash.com/photo-1523050853051-f75dbba892c1?w=800", // Genérica univ
        "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800", // Aula 2
        "https://images.unsplash.com/photo-1562774053-701939374585?w=800", // Lab info
        "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=800", // Clase moderna
        "https://images.unsplash.com/photo-1498243639359-f7c8928ee1cf?w=800", // Estudio
        "https://images.unsplash.com/photo-1503428593586-e225b39bddfe?w=800"  // Biblioteca
    };

    public static String getUrlPorIdTipo(Integer idTipo) {
        if (idTipo == null || idTipo < 0) return URLS_EDUCATIVAS[0];
        // Usar el operador módulo para que siempre asigne una imagen del array
        int index = idTipo % URLS_EDUCATIVAS.length;
        return URLS_EDUCATIVAS[index];
    }
}
