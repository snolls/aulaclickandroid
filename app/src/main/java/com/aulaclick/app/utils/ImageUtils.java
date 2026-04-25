package com.aulaclick.app.utils;

public class ImageUtils {
    public static final String[] IMAGENES_DEFAULT = {
        "https://images.unsplash.com/photo-1541339907198-e08756ebafe3?w=800", // Aula clásica universitaria
        "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=800", // Laboratorio tecnológico
        "https://images.unsplash.com/photo-1497366754035-f200968a6e72?w=800"  // Sala de juntas moderna
    };

    public static String getImagenForRecurso(Integer id) {
        int index = id != null ? id % 3 : 0;
        return IMAGENES_DEFAULT[index];
    }
}
