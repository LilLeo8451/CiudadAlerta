package com.example.ciudadalerta.data.remote.api

import com.example.ciudadalerta.data.remote.dto.ReporteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReporteApiService {

    @GET("reportes")
    suspend fun listar(): List<ReporteDto>

    @GET("reportes")
    suspend fun listarPorUsuario(@Query("usuarioId") usuarioId: Int): List<ReporteDto>

    @GET("reportes/{id}")
    suspend fun obtener(@Path("id") id: Int): ReporteDto

    @POST("reportes")
    suspend fun crear(@Body reporte: ReporteDto): ReporteDto

    @PUT("reportes/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body reporte: ReporteDto): ReporteDto

    @DELETE("reportes/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}
