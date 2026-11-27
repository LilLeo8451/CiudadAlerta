package com.example.ciudadalerta.data.remote.api

import com.example.ciudadalerta.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Endpoints del recurso "usuarios"
interface UsuarioApiService {

    @GET("usuarios")
    suspend fun listar(): List<UsuarioDto>

    // MockAPI responde 404 cuando el filtro no encuentra nada, por eso Response
    @GET("usuarios")
    suspend fun buscarPorCorreo(@Query("correo") correo: String): Response<List<UsuarioDto>>

    @POST("usuarios")
    suspend fun crear(@Body usuario: UsuarioDto): UsuarioDto
}
