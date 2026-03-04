package com.aulaclick.app.network;

import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.LoginRequest;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.TipoRecurso;
import com.aulaclick.app.network.models.UsuarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AulaclickApiService {
    @POST("api/usuarios/login")
    Call<UsuarioResponse> login(@Body LoginRequest request);

    @GET("api/recursos")
    Call<List<Recurso>> getRecursos();

    @POST("api/recursos")
    Call<Recurso> crearRecurso(@Body Recurso recurso);

    @GET("api/departamentos")
    Call<List<Departamento>> getDepartamentos();

    @POST("api/departamentos")
    Call<Departamento> crearDepartamento(@Body Departamento departamento);

    @DELETE("api/departamentos/{id}")
    Call<Void> eliminarDepartamento(@Path("id") Integer id);

    @GET("api/tipos-recurso")
    Call<List<TipoRecurso>> getTiposRecurso();

    @POST("api/tipos-recurso")
    Call<TipoRecurso> crearTipoRecurso(@Body TipoRecurso tipoRecurso);

    @DELETE("api/tipos-recurso/{id}")
    Call<Void> eliminarTipoRecurso(@Path("id") Integer id);

    @GET("api/equipamientos")
    Call<List<Equipamiento>> getEquipamientos();

    @POST("api/equipamientos")
    Call<Equipamiento> crearEquipamiento(@Body Equipamiento equipamiento);

    @DELETE("api/equipamientos/{id}")
    Call<Void> eliminarEquipamiento(@Path("id") Integer id);
}
