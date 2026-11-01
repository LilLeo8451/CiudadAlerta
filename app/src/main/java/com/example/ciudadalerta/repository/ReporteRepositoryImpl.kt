package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.NotificacionDao
import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.NotificacionEntity
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.local.toEntity
import com.example.ciudadalerta.data.local.toModel
import com.example.ciudadalerta.data.remote.ReporteRemoteDataSource
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReporteRepositoryImpl(
    private val reporteDao: ReporteDao,
    private val notificacionDao: NotificacionDao,
    private val remote: ReporteRemoteDataSource
) : ReporteRepository {

    // ---------- Lecturas: siempre desde Room ----------
    override fun observarTodos(): Flow<List<Reporte>> =
        reporteDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    override fun observarPorUsuario(usuarioId: Int): Flow<List<Reporte>> =
        reporteDao.observarPorUsuario(usuarioId).map { lista -> lista.map { it.toModel() } }

    override fun observarPorId(id: Int): Flow<Reporte?> =
        reporteDao.observarPorId(id).map { it?.toModel() }

    // ---------- Escrituras: primero Room, luego se intenta subir ----------
    override suspend fun crear(reporte: Reporte): Int {
        val idLocal = reporteDao.insertar(
            reporte.toEntity().copy(id = 0, sincronizado = false, remoteId = null)
        ).toInt()
        reporteDao.buscarPorId(idLocal)?.let { subir(it) }
        return idLocal
    }

    override suspend fun editar(reporte: Reporte) {
        reporteDao.actualizar(reporte.toEntity().copy(sincronizado = false))
        reporteDao.buscarPorId(reporte.id)?.let { subir(it) }
    }

    override suspend fun cambiarEstado(id: Int, estado: EstadoReporte) {
        val actual = reporteDao.buscarPorId(id) ?: return
        if (actual.estatus == estado) return

        reporteDao.actualizarEstado(id, estado)   // deja sincronizado = false
        notificacionDao.insertar(
            NotificacionEntity(usuarioId = actual.usuarioId, reporteId = id, estadoNuevo = estado)
        )

        val remoteId = actual.remoteId
        if (remoteId != null) {
            if (remote.cambiarEstado(remoteId, estado).isSuccess) reporteDao.marcarSincronizado(id, remoteId)
        } else {
            reporteDao.buscarPorId(id)?.let { subir(it) }
        }
    }

    override suspend fun eliminar(id: Int) {
        val reporte = reporteDao.buscarPorId(id) ?: return
        reporteDao.eliminarPorId(id)               // sus fotos y notificaciones se borran en cascada
        reporte.remoteId?.let { remote.eliminar(it) }
    }

    // ---------- Sincronización ----------
    override suspend fun refrescarDesdeServidor(usuarioId: Int?): Result<Unit> {
        val remotos = remote.listar(usuarioId).getOrElse { return Result.failure(it) }

        for (remoto in remotos) {
            val remoteId = remoto.remoteId ?: continue
            val local = reporteDao.buscarPorRemoteId(remoteId)

            if (local == null) {
                reporteDao.insertar(remoto.toEntity().copy(id = 0, sincronizado = true))
            } else if (local.sincronizado) {       // si hay cambios locales sin subir, esos ganan
                if (local.estatus != remoto.estatus) {
                    // El estado cambió en el servidor: se avisa al autor del reporte
                    notificacionDao.insertar(
                        NotificacionEntity(
                            usuarioId = local.usuarioId,
                            reporteId = local.id,
                            estadoNuevo = remoto.estatus
                        )
                    )
                }
                reporteDao.actualizar(remoto.toEntity().copy(id = local.id, sincronizado = true))
            }
        }
        return Result.success(Unit)
    }

    override suspend fun sincronizarPendientes(): Result<Unit> {
        var fallos = 0
        for (pendiente in reporteDao.obtenerPendientesDeSincronizar()) {
            if (!subir(pendiente)) fallos++
        }
        return if (fallos == 0) Result.success(Unit)
        else Result.failure(Exception("$fallos reportes siguen pendientes de subir"))
    }

    /** Sube un reporte (POST si es nuevo, PATCH si ya existe en el servidor). true = quedó sincronizado. */
    private suspend fun subir(entity: ReporteEntity): Boolean {
        val resultado = if (entity.remoteId == null) remote.crear(entity.toModel())
        else remote.actualizar(entity.toModel())

        val subido = resultado.getOrNull() ?: return false
        val remoteId = subido.remoteId ?: entity.remoteId ?: return false
        reporteDao.marcarSincronizado(entity.id, remoteId)
        return true
    }
}