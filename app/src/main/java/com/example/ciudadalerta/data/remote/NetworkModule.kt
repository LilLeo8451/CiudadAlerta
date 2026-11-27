package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.api.CloudinaryApiService
import com.example.ciudadalerta.data.remote.api.ReporteApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    const val BASE_URL = "https://6ae178d0be3bc8fead98893d.mockapi.io/api/v1/"

    const val TIMEOUT_SEGUNDOS = 15L
}

// Cloudinary: fotos en la nube (preset sin firma, no requiere clave secreta)
object CloudinaryConfig {
    const val BASE_URL = "https://api.cloudinary.com/v1_1/"
    const val CLOUD_NAME = "ujqvxmq5"
    const val UPLOAD_PRESET = "ciudadalerta_fotos"

    const val TIMEOUT_SEGUNDOS = 60L   // subir una foto tarda más que un JSON
}

object NetworkModule {

    private val gson: Gson = GsonBuilder().create()

    // Imprime en Logcat cada petición y respuesta con su JSON
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

    // Cliente aparte para fotos: solo registra encabezados (no el archivo completo)
    private val clienteFotos: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
            .connectTimeout(CloudinaryConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(CloudinaryConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(CloudinaryConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .build()
    }

    val cloudinaryApi: CloudinaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CloudinaryConfig.BASE_URL)
            .client(clienteFotos)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CloudinaryApiService::class.java)
    }
}
