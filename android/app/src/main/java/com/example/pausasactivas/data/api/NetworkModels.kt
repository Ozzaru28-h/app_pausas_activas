package com.example.pausasactivas.data.api

import com.google.gson.annotations.SerializedName

/**
 * Envoltorio de las respuestas exitosas de la API: { exito, mensaje, total, datos }.
 * Los campos son opcionales porque Gson no valida nulos de Kotlin.
 */
data class ApiEnvelope<T>(
    @SerializedName("exito") val exito: Boolean? = null,
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("datos") val datos: T? = null
)

/** Cuerpo de error de la API: { mensaje, error }. */
data class ApiErrorBody(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("error") val error: String? = null
)

// ----------------------------------------------------------------- Usuarios

data class CreateUserRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("carrera") val carrera: String? = null
)

data class UserDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("carrera") val carrera: String? = null
)

// ----------------------------------------------------------------- Sesiones

data class ExerciseRefDto(
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class MetricErrorDto(
    @SerializedName("articulacion_falla") val articulacionFalla: String? = null,
    @SerializedName("angulo_detectado") val anguloDetectado: Double? = null,
    @SerializedName("angulo_esperado") val anguloEsperado: Double? = null
)

data class TelemetryDto(
    @SerializedName("postura_valida") val posturaValida: Boolean? = null,
    @SerializedName("precision_promedio_pct") val precisionPromedioPct: Double? = null,
    @SerializedName("metricas_error") val metricasError: List<MetricErrorDto>? = null
)

/** Cuerpo de POST /api/sesiones. */
data class SessionRequest(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("ejercicio") val ejercicio: ExerciseRefDto,
    @SerializedName("telemetria_ia") val telemetria: TelemetryDto,
    @SerializedName("notas_usuario") val notasUsuario: String = ""
)

/** Sesión guardada, tal como la devuelve la API. */
data class SessionDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String? = null,
    @SerializedName("ejercicio") val ejercicio: ExerciseRefDto? = null,
    @SerializedName("telemetria_ia") val telemetria: TelemetryDto? = null,
    @SerializedName("notas_usuario") val notasUsuario: String? = null
)
