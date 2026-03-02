package com.aulaclick.app.network;

import com.aulaclick.app.network.models.LoginRequest;
import com.aulaclick.app.network.models.UsuarioResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AulaclickApiService {
    @POST("api/usuarios/login")
    Call<UsuarioResponse> login(@Body LoginRequest request);
}
