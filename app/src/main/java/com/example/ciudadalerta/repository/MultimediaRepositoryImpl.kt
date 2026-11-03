package com.example.ciudadalerta.repository

import android.net.Uri
import com.example.ciudadalerta.data.local.dao.MultimediaDao
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.data.mapper.toEntity
import com.example.ciudadalerta.model.Multimedia
import com.example.ciudadalerta.util.AlmacenFotos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Fotos de los reportes: el archivo vive en el almacenamiento privado de la app (AlmacenFotos)
 * y Room guarda la ruta. Por ahora no se suben a la Web API (sincronizado = false).
 */
class MultimediaRepositoryImpl(
    private val dao: MultimediaDao,
    private val almacen: AlmacenFotos
) : MultimediaRepository {

    override fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>> =
        dao.observarPorReporte(reporteId).map { lista -> lista.map { it.toDomain() } }

    override suspend fun agregar(reporteId: Int, uri: String): Int =
        dao.insertar(MultimediaEntity(reporteId = reporteId, uri = uri)).toInt()

    override fun crearUriParaCamara(): Uri = almacen.crearUriParaCamara()

    override suspend fun agregarFoto(reporteId: Int, origen: Uri): Int {
        val ruta = almacen.guardar(origen)
        return agregar(reporteId, ruta)
    }

    override suspend fun eliminar(multimedia: Multimedia) {
        dao.eliminar(multimedia.toEntity())
        almacen.eliminar(multimedia.uri)
    }

    override suspend fun eliminarArchivosDeReporte(reporteId: Int) {
        dao.observarPorReporte(reporteId).first().forEach { almacen.eliminar(it.uri) }
    }
}
