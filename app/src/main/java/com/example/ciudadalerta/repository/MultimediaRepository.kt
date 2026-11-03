package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.Multimedia
import kotlinx.coroutines.flow.Flow

interface MultimediaRepository {

    fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>>

    /** Guarda la foto en Room (sincronizado = false). Devuelve el id local. */
    suspend fun agregar(reporteId: Int, uri: String): Int

    suspend fun eliminar(multimedia: Multimedia)
}