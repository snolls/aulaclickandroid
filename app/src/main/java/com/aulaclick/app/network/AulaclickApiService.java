package com.aulaclick.app.network;

import com.aulaclick.app.network.models.AdminCambiarPasswordDTO;
import com.aulaclick.app.network.models.CambioPasswordDTO;
import com.aulaclick.app.network.models.EstadisticasDTO;
import com.aulaclick.app.network.models.RolDTO;
import com.aulaclick.app.network.models.SedeDTO;
import com.aulaclick.app.network.models.UsuarioDTO;
import com.aulaclick.app.network.models.UsuarioRequestDTO;
import com.aulaclick.app.network.models.UsuarioRolSedeDTO;
import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.ImagenGaleria;
import com.aulaclick.app.network.models.ImagenGaleriaDTO;
import com.aulaclick.app.network.models.ImagenRequestDTO;
import com.aulaclick.app.network.models.LoginRequest;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.RecursoRequestDTO;
import com.aulaclick.app.network.models.ReservaRequestDTO;
import com.aulaclick.app.network.models.TipoRecurso;
import com.aulaclick.app.network.models.ReservaDTO;
import com.aulaclick.app.network.models.UsuarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AulaclickApiService {
    @POST("api/usuarios/login")
    Call<UsuarioResponse> login(@Body LoginRequest request);

    @PUT("api/usuarios/{id}/cambiar-password")
    Call<Void> cambiarPassword(@Path("id") long idUsuario, @Body CambioPasswordDTO dto);

    @PUT("api/usuarios/{id}/admin-cambiar-password")
    Call<Void> adminCambiarPassword(@Path("id") Long id, @Body AdminCambiarPasswordDTO dto);

    @GET("api/admin/estadisticas")
    Call<EstadisticasDTO> getEstadisticas(@retrofit2.http.Query("sedeId") Long sedeId);

    @GET("api/usuarios")
    Call<List<UsuarioDTO>> getUsuarios();

    @GET("api/usuarios/verificar-email")
    Call<Void> verificarEmail(@retrofit2.http.Query("email") String email);

    @POST("api/usuarios")
    Call<UsuarioDTO> crearUsuario(@Body UsuarioRequestDTO dto);

    @PUT("api/usuarios/{id}/rol-sede")
    Call<UsuarioDTO> actualizarRolSede(@Path("id") Long id, @Body UsuarioRolSedeDTO dto);

    @GET("api/sedes")
    Call<List<SedeDTO>> getSedes();

    @POST("api/sedes")
    Call<SedeDTO> crearSede(@Body SedeDTO sede);

    @PUT("api/sedes/{id}")
    Call<SedeDTO> actualizarSede(@Path("id") Long id, @Body SedeDTO sede);

    @DELETE("api/sedes/{id}")
    Call<Void> eliminarSede(@Path("id") Long id);

    @GET("api/roles")
    Call<List<RolDTO>> getRoles();

    @GET("api/recursos")
    Call<List<Recurso>> getRecursos();

    @POST("api/recursos")
    Call<Recurso> crearRecurso(@Header("X-Rol-Usuario") String rolUsuario, @Body RecursoRequestDTO recurso);

    @GET("api/recursos/{id}")
    Call<Recurso> getRecurso(@Path("id") Long id);

    @PUT("api/recursos/{id}")
    Call<Recurso> actualizarRecurso(@Header("X-Rol-Usuario") String rolUsuario, @Path("id") Long id, @Body RecursoRequestDTO recurso);

    @DELETE("api/recursos/{id}")
    Call<Void> eliminarRecurso(@Header("X-Rol-Usuario") String rolUsuario, @Path("id") int id);

    @GET("api/reservas/admin")
    Call<List<ReservaDTO>> getReservasAdmin(@retrofit2.http.Query("sedeId") Long sedeId);

    @GET("api/reservas/recurso/{idRecurso}")
    Call<List<ReservaDTO>> getReservasPorRecurso(@Path("idRecurso") Long idRecurso);

    @GET("api/reservas/usuario/{id}")
    Call<List<ReservaDTO>> getMisReservas(@Path("id") Long idUsuario);

    @PUT("api/reservas/{id}/cancelar")
    Call<ReservaDTO> cancelarReserva(@Path("id") Long idReserva, @Body com.google.gson.JsonObject emptyBody);

    @POST("api/reservas")
    Call<ReservaDTO> crearReserva(@Body ReservaRequestDTO request);

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

    @PUT("api/tipos-recurso/{id}")
    Call<TipoRecurso> actualizarTipoRecurso(@Path("id") Integer id, @Body TipoRecurso tipoRecurso);

    @DELETE("api/tipos-recurso/{id}")
    Call<Void> eliminarTipoRecurso(@Path("id") Integer id);

    @GET("api/recursos/imagenes")
    Call<List<ImagenGaleriaDTO>> getImagenesExistentes();

    @POST("api/recursos/imagenes")
    Call<ImagenGaleriaDTO> registrarImagenEnGaleria(@Body ImagenRequestDTO dto);

    @DELETE("api/recursos/imagenes/{id}")
    Call<Void> eliminarImagenGaleria(@Path("id") Long id);

    @DELETE("api/recursos/imagenes")
    Call<Void> eliminarImagenesMasivo(@retrofit2.http.Query("ids") java.util.List<Long> ids);

    @POST("api/recursos/imagenes/batch")
    Call<List<ImagenGaleriaDTO>> registrarImagenesMasivo(@Body List<ImagenRequestDTO> dtos);

    @GET("api/equipamientos")
    Call<List<Equipamiento>> getEquipamientos();

    @POST("api/equipamientos")
    Call<Equipamiento> crearEquipamiento(@Body Equipamiento equipamiento);

    @DELETE("api/equipamientos/{id}")
    Call<Void> eliminarEquipamiento(@Path("id") Integer id);

    @DELETE("api/usuarios/{id}")
    Call<Void> eliminarUsuario(@Path("id") Long id);
}
