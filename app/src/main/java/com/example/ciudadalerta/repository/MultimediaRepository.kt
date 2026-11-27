package com.example.ciudadalerta.repository

import android.net.Uri
import com.example.ciudadalerta.model.Multimedia
import kotlinx.coroutines.flow.Flow

interface MultimediaRepository {

    fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>>

    suspend fun agregar(reporteId: Int, uri: String): Int

    fun crearUriParaCamara(): Uri

    suspend fun agregarFoto(reporteId: Int, origen: Uri): Int

    suspend fun eliminar(multimedia: Multimedia)

    suspend fun eliminarArchivosDeReporte(reporteId: Int)

    // Sube a Cloudinary las fotos que faltan y regresa todas las URLs del reporte
    suspend fun subirPendientes(reporteId: Int): Result<List<String>>

    // Deja las fotos del reporte igual que en el servidor
    suspend fun aplicarFotosDelServidor(reporteId: Int, urls: List<String>)
}
