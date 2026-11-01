package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.MultimediaDao
import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.local.toEntity
import com.example.ciudadalerta.data.local.toModel
import com.example.ciudadalerta.data.remote.ReporteRemoteDataSource
import com.example.ciudadalerta.model.Multimedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MultimediaRepositoryImpl(
    private val multimediaDao: MultimediaDao,
    private val reporteDao: ReporteDao,
    private val remote: ReporteRemoteDataSource
) : MultimediaRepository {

    override fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>> =
        multimediaDao.observarPorReporte(reporteId).map { lista -> lista.map { it.toModel() } }

    override suspend fun agregar(reporteId: Int, uri: String): Int =
        multimediaDao.insertar(MultimediaEntity(reporteId = reporteId, uri = uri)).toInt()

    override suspend fun eliminar(multimedia: Multimedia) {
        multimediaDao.eliminar(multimedia.toEntity())
    }

    override suspend fun sincronizarPendientes(): Result<Unit> {
        var fallos = 0
        for (foto in multimediaDao.obtenerPendientesDeSincronizar()) {
            // Una foto solo se puede subir cuando su reporte ya existe en el servidor
            val remoteReporteId = reporteDao.buscarPorId(foto.reporteId)?.remoteId
            if (remoteReporteId == null) {
                fallos++
                continue
            }
            if (remote.subirFoto(remoteReporteId, foto.uri).isSuccess) {
                multimediaDao.marcarSincronizada(foto.id)
            } else {
                fallos++
            }
        }
        return if (fallos == 0) Result.success(Unit)
        else Result.failure(Exception("$fallos fotos siguen pendientes de subir"))
    }
}