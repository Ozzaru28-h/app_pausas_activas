package com.example.pausasactivas.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints reales del backend Node.js/Express (ver carpeta routes/ en la raíz del repo).
 */
interface ApiService {

    /** POST /api/usuarios — registra un usuario (correo @uniautonoma.edu.co). */
    @POST("api/usuarios")
    suspend fun createUser(
        @Body body: CreateUserRequest
    ): Response<ApiEnvelope<UserDto>>

    /** GET /api/usuarios/buscar?correo=... — acceso de demo (sin contraseña). */
    @GET("api/usuarios/buscar")
    suspend fun findUserByEmail(
        @Query("correo") correo: String
    ): Response<ApiEnvelope<UserDto>>

    /** POST /api/sesiones — guarda la telemetría de una pausa activa. */
    @POST("api/sesiones")
    suspend fun saveSession(
        @Body body: SessionRequest
    ): Response<ApiEnvelope<SessionDto>>

    /** GET /api/sesiones/usuario/:usuario_id — historial del usuario (más recientes primero). */
    @GET("api/sesiones/usuario/{usuarioId}")
    suspend fun getSessionsByUser(
        @Path("usuarioId") usuarioId: String
    ): Response<ApiEnvelope<List<SessionDto>>>
}
