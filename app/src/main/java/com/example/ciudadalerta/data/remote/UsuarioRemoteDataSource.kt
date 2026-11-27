package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.api.UsuarioApiService
import com.example.ciudadalerta.data.remote.dto.UsuarioDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

// Fuente REMOTA de usuarios (MockAPI)
class UsuarioRemoteDataSource(private val api: UsuarioApiService) {

    suspend fun listar(): Result<List<UsuarioDto>> = seguro { api.listar() }

    // success(null) = no existe; failure = no se pudo consultar
    suspend fun buscarPorCorreo(correo: String): Result<UsuarioDto?> = seguro {
        val respuesta = api.buscarPorCorreo(correo)
        when {
            respuesta.code() == 404 -> null
            !respuesta.isSuccessful -> throw HttpException(respuesta)
            // El filtro de MockAPI es por coincidencia parcial: se compara exacto
            else -> respuesta.body().orEmpty().firstOrNull { it.correo.equals(correo, ignoreCase = true) }
        }
    }

    suspend fun crear(usuario: UsuarioDto): Result<UsuarioDto> = seguro { api.crear(usuario) }

    private suspend fun <T> seguro(peticion: suspend () -> T): Result<T> =
        try {
            Result.success(peticion())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
