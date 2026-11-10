package com.example.pausasactivas.data.api

import com.google.gson.annotations.SerializedName

/**
 * DTO para enviar el resumen de la pausa activa finalizada a la API Node.js.
 */
data class SessionReportRequest(
    @SerializedName("userId") val userId: String = "user_default",
    @SerializedName("exerciseName") val exerciseName: String,
    @SerializedName("repetitions") val repetitions: Int,
    @SerializedName("durationSeconds") val durationSeconds: Int,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO para la respuesta del servidor Node.js.
 */
data class SessionReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
