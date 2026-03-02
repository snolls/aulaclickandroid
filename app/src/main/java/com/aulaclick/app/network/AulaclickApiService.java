package com.aulaclick.app.network;

import com.aulaclick.app.network.models.LoginRequest;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.UsuarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AulaclickApiService {
    @POST("api/usuarios/login")
    Call<UsuarioResponse> login(@Body LoginRequest request);

    @GET("api/recursos")
    Call<List<Recurso>> getRecursos();

    @POST("api/recursos")
    Call<Recurso> crearRecurso(@Body Recurso recurso);
}
