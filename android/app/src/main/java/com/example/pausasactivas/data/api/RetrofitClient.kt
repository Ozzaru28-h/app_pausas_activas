package com.example.pausasactivas.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * URL base de la API Node.js (puerto 3000).
     *  - Emulador Android: http://10.0.2.2:3000/  (10.0.2.2 es el "localhost" del computador).
     *  - Teléfono físico: cambia a la IP local del computador, p. ej. "http://192.168.1.25:3000/"
     *    (el teléfono y el computador deben estar en la misma red Wi-Fi).
     */
    const val BASE_URL = "http://10.0.2.2:3000/"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
