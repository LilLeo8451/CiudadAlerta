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
}
