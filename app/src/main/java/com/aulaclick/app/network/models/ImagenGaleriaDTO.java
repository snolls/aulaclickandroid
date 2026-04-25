package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class ImagenGaleriaDTO {
    @SerializedName("idImagen")
    private Long idImagen;

    @SerializedName("url")
    private String url;

    public ImagenGaleriaDTO() {}

    public Long getIdImagen() { return idImagen; }
    public void setIdImagen(Long idImagen) { this.idImagen = idImagen; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
