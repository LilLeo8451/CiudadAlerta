package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.api.ReporteApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    /**
     * URL base de la Web API. DEBE terminar en "/".
     *  - MockAPI:                      "https://<tu-id>.mockapi.io/api/v1/"
     *  - json-server en el emulador:   "http://10.0.2.2:3000/"  (ver API.md)
     */
    const val BASE_URL = "https://6ae178d0be3bc8fead98893d.mockapi.io/api/v1/"

    const val TIMEOUT_SEGUNDOS = 15L
}

object NetworkModule {

    private val gson: Gson = GsonBuilder().create()

    /** Imprime en Logcat (tag "okhttp.OkHttpClient") cada petición y respuesta con su JSON. */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val reporteApi: ReporteApiService by lazy { retrofit.create(ReporteApiService::class.java) }
}
