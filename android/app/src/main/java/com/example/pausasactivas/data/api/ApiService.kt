package com.example.pausasactivas.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    /**
     * Envía las métricas de la pausa activa al backend Node.js.
     */
    @POST("api/pausas/report")
    suspend fun sendSessionReport(
        @Body report: SessionReportRequest
    ): Response<SessionReportResponse>
}
