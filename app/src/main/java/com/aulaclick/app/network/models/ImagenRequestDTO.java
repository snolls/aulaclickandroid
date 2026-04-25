package com.aulaclick.app.network.models;

import com.google.gson.annotations.SerializedName;

public class ImagenRequestDTO {
    @SerializedName("url")
    private String url;

    public ImagenRequestDTO() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
