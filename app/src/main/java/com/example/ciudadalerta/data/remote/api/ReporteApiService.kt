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

/**
 * Endpoints REST del recurso "reportes".
 * Las rutas son relativas a ApiConfig.BASE_URL (por eso no empiezan con "/").
 */
interface ReporteApiService {

    /** Listar: GET /reportes */
    @GET("reportes")
    suspend fun listar(): List<ReporteDto>

    /** Listar los de un ciudadano: GET /reportes?usuarioId=2 */
    @GET("reportes")
    suspend fun listarPorUsuario(@Query("usuarioId") usuarioId: Int): List<ReporteDto>

    /** Ver / recuperar uno: GET /reportes/{id} */
    @GET("reportes/{id}")
    suspend fun obtener(@Path("id") id: Int): ReporteDto

    /** Agregar: POST /reportes  → el servidor responde el reporte con su id */
    @POST("reportes")
    suspend fun crear(@Body reporte: ReporteDto): ReporteDto

    /** Editar (incluye cambio de estatus): PUT /reportes/{id} */
    @PUT("reportes/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body reporte: ReporteDto): ReporteDto

    /** Eliminar: DELETE /reportes/{id}. Se usa Response para poder revisar el código HTTP. */
    @DELETE("reportes/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}
