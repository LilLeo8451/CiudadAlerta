package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte

interface ReporteRemoteDataSource {

    /** GET /reportes  (o GET /reportes?usuarioId=...). Devuelve reportes con remoteId lleno. */
    suspend fun listar(usuarioId: Int? = null): Result<List<Reporte>>

    /** GET /reportes/{id} */
    suspend fun obtener(remoteId: Int): Result<Reporte>

    /** POST /reportes. Devuelve el reporte con el remoteId que asignó el servidor. */
    suspend fun crear(reporte: Reporte): Result<Reporte>

    /** PATCH /reportes/{id} con los campos editables. */
    suspend fun actualizar(reporte: Reporte): Result<Reporte>

    /** PATCH /reportes/{id} solo con el estado (lo usa el administrador). */
    suspend fun cambiarEstado(remoteId: Int, estado: EstadoReporte): Result<Reporte>

    /** DELETE /reportes/{id} */
    suspend fun eliminar(remoteId: Int): Result<Unit>

    /** POST /reportes/{id}/fotos (multipart). uri = ruta o URI local de la foto. */
    suspend fun subirFoto(remoteReporteId: Int, uri: String): Result<Unit>
}