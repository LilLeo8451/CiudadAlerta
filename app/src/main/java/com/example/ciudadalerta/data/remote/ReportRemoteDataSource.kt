package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.api.ReporteApiService
import com.example.ciudadalerta.data.remote.dto.ReporteDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

// Fuente REMOTA de reportes
class ReportRemoteDataSource(private val api: ReporteApiService) {

    suspend fun listar(): Result<List<ReporteDto>> = llamadaSegura { api.listar() }

    suspend fun listarPorUsuario(usuarioId: Int): Result<List<ReporteDto>> =
        llamadaSegura { api.listarPorUsuario(usuarioId) }

    suspend fun obtener(remoteId: Int): Result<ReporteDto> = llamadaSegura { api.obtener(remoteId) }

    suspend fun crear(reporte: ReporteDto): Result<ReporteDto> = llamadaSegura { api.crear(reporte) }

    suspend fun actualizar(remoteId: Int, reporte: ReporteDto): Result<ReporteDto> =
        llamadaSegura { api.actualizar(remoteId, reporte) }

    suspend fun eliminar(remoteId: Int): Result<Unit> = llamadaSegura {
        val respuesta = api.eliminar(remoteId)
        // 404 = ya no existía en el servidor, para nosotros también cuenta como eliminado
        if (!respuesta.isSuccessful && respuesta.code() != 404) throw HttpException(respuesta)
    }
}

//true si el error fue un 404 del servidor
fun Throwable.esNoEncontrado(): Boolean = this is HttpException && code() == 404

private suspend fun <T> llamadaSegura(peticion: suspend () -> T): Result<T> =
    try {
        Result.success(peticion())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
