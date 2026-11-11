package com.example.pausasactivas.data.repository

import com.example.pausasactivas.data.api.ApiEnvelope
import com.example.pausasactivas.data.api.ApiErrorBody
import com.example.pausasactivas.data.api.ApiService
import com.example.pausasactivas.data.api.CreateUserRequest
import com.example.pausasactivas.data.api.RetrofitClient
import com.example.pausasactivas.data.api.SessionDto
import com.example.pausasactivas.data.api.SessionRequest
import com.example.pausasactivas.data.api.UserDto
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Error con un mensaje ya listo para mostrarse al usuario. */
class ApiException(message: String) : Exception(message)

class PausasRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    /** Acceso de demo: busca al usuario por correo (sin contraseña). */
    suspend fun login(correo: String): Result<UserDto> =
        call { api.findUserByEmail(correo) }.toValidUser()

    suspend fun register(correo: String, nombre: String, carrera: String?): Result<UserDto> =
        call { api.createUser(CreateUserRequest(correo, nombre, carrera)) }.toValidUser()

    suspend fun saveSession(request: SessionRequest): Result<Unit> =
        call { api.saveSession(request) }.map { }

    suspend fun getHistory(userId: String): Result<List<SessionDto>> =
        call { api.getSessionsByUser(userId) }.map { it.datos ?: emptyList() }

    // ------------------------------------------------------------------ Helpers

    private fun Result<ApiEnvelope<UserDto>>.toValidUser(): Result<UserDto> = mapCatching { envelope ->
        val user = envelope.datos
        if (user == null || user.id.isNullOrBlank()) {
            throw ApiException("El servidor devolvió datos de usuario incompletos.")
        }
        user
    }

    private suspend fun <T> call(block: suspend () -> Response<T>): Result<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    Result.failure(ApiException("El servidor devolvió una respuesta vacía."))
                } else {
                    Result.success(body)
                }
            } else {
                Result.failure(ApiException(errorMessage(response)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(
                ApiException(
                    "No se pudo conectar con el servidor. Verifica que la API esté encendida " +
                        "(node index.js) y que la URL en RetrofitClient.kt sea la correcta."
                )
            )
        } catch (e: Exception) {
            Result.failure(ApiException("Respuesta inesperada del servidor."))
        }
    }

    private fun errorMessage(response: Response<*>): String {
        val raw = try {
            response.errorBody()?.string()
        } catch (e: IOException) {
            null
        }
        val serverMessage = try {
            Gson().fromJson(raw, ApiErrorBody::class.java)?.mensaje
        } catch (e: Exception) {
            null
        }
        return when {
            !serverMessage.isNullOrBlank() -> serverMessage
            response.code() == 404 ->
                "No se encontró el recurso en el servidor. Verifica que el backend esté " +
                    "actualizado y reiniciado."
            else -> "El servidor respondió con un error (código ${response.code()})."
        }
    }
}
