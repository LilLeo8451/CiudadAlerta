package com.example.ciudadalerta.repository

import android.net.Uri
import com.example.ciudadalerta.data.local.dao.MultimediaDao
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.data.mapper.toEntity
import com.example.ciudadalerta.data.remote.FotosRemoteDataSource
import com.example.ciudadalerta.model.Multimedia
import com.example.ciudadalerta.util.AlmacenFotos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Fotos de los reportes: el archivo vive en el almacenamiento privado de la app
class MultimediaRepositoryImpl(
    private val dao: MultimediaDao,
    private val almacen: AlmacenFotos,
    private val remote: FotosRemoteDataSource
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

    override suspend fun subirPendientes(reporteId: Int): Result<List<String>> {
        dao.obtenerPorReporte(reporteId)
            .filter { it.urlRemota == null }
            .forEach { foto ->
                val archivo = almacen.archivoDe(foto.uri) ?: return@forEach
                val url = remote.subir(archivo).getOrElse { return Result.failure(it) }
                dao.marcarSubida(foto.id, url)
            }
        return Result.success(dao.obtenerPorReporte(reporteId).mapNotNull { it.urlRemota })
    }

    override suspend fun aplicarFotosDelServidor(reporteId: Int, urls: List<String>) {
        val actuales = dao.obtenerPorReporte(reporteId)
        val conocidas = actuales.mapNotNull { it.urlRemota }.toSet()

        // Nuevas: se muestran directo desde la URL
        urls.filterNot { it in conocidas }.forEach { url ->
            dao.insertar(MultimediaEntity(reporteId = reporteId, uri = url, sincronizado = true, urlRemota = url))
        }
        // Las que ya no están en el servidor se quitaron en otro teléfono
        actuales.filter { it.urlRemota != null && it.urlRemota !in urls }.forEach { foto ->
            dao.eliminarPorId(foto.id)
            almacen.eliminar(foto.uri)
        }
    }
}
