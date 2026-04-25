package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class ImagenGaleria {
    @SerializedName("id")
    private Long id;

    @SerializedName("url")
    private String url;

    public ImagenGaleria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
