package com.example.ciudadalerta.repository

import android.net.Uri
import com.example.ciudadalerta.model.Multimedia
import kotlinx.coroutines.flow.Flow

interface MultimediaRepository {

    /** Listar las fotos de un reporte (se actualiza solo cuando cambia la tabla). */
    fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>>

    /** Guarda en Room una foto que ya está en el almacenamiento de la app. Devuelve el id local. */
    suspend fun agregar(reporteId: Int, uri: String): Int

    /** Uri temporal para que la app de cámara escriba la foto. */
    fun crearUriParaCamara(): Uri

    /**
     * Agregar foto: copia la imagen (cámara o galería) al almacenamiento de la app
     * y la registra en Room. Devuelve el id local.
     */
    suspend fun agregarFoto(reporteId: Int, origen: Uri): Int

    /** Eliminar una foto: borra la fila de Room y el archivo. */
    suspend fun eliminar(multimedia: Multimedia)

    /**
     * Borra los archivos de todas las fotos de un reporte.
     * Llamar ANTES de eliminar el reporte (las filas se borran solas por el CASCADE).
     */
    suspend fun eliminarArchivosDeReporte(reporteId: Int)
}
