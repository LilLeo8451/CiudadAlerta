package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.ReportLocalDataSource
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.data.mapper.toDto
import com.example.ciudadalerta.data.mapper.toEntity
import com.example.ciudadalerta.data.mapper.toEntityOrNull
import com.example.ciudadalerta.data.remote.ReportRemoteDataSource
import com.example.ciudadalerta.data.remote.esNoEncontrado
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ReportRepositoryImpl(
    private val local: ReportLocalDataSource,
    private val remote: ReportRemoteDataSource,
    private val notificaciones: NotificacionRepository
) : ReportRepository {
    // Evita que dos sincronizaciones al mismo tiempo suban dos veces el mismo reporte
    private val mutex = Mutex()

    override val reportes: Flow<List<Reporte>> =
        local.observarTodos().map { lista -> lista.map { it.toDomain() } }

    override fun observarPorUsuario(usuarioId: Int): Flow<List<Reporte>> =
        local.observarPorUsuario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    override fun observarPorId(id: Int): Flow<Reporte?> =
        local.observarPorId(id).map { it?.toDomain() }

    override suspend fun crear(reporte: Reporte): Int {
        val idLocal = local.insertar(reporte.toEntity().copy(id = 0))
        subir(idLocal)
        return idLocal
    }

    override suspend fun actualizar(reporte: Reporte) {
        val actual = local.buscarPorId(reporte.id) ?: return
        // Conservamos lo que el modelo de dominio no trae
        local.actualizar(
            reporte.toEntity(
                latitud = actual.latitud,
                longitud = actual.longitud,
                remoteId = actual.remoteId
            )
        )
        subir(reporte.id)
    }

    override suspend fun cambiarEstado(id: Int, estado: EstadoReporte) {
        val actual = local.buscarPorId(id) ?: return
        if (actual.estatus == estado) return

        local.actualizarEstado(id, estado)   // el DAO ya lo marca sincronizado = 0
        notificaciones.crear(actual.usuarioId, id, estado)   // avisa al autor del reporte
        subir(id)
    }

    override suspend fun eliminar(id: Int) {
        // Termina en Room y en la API aunque se cierre la pantalla
        withContext(NonCancellable) {
            val reporte = local.buscarPorId(id) ?: return@withContext
            local.eliminarPorId(id)
            reporte.remoteId?.let { remote.eliminar(it) }
        }
    }

    override suspend fun sincronizar(): Result<Unit> {
        local.pendientesDeSincronizar().forEach { subir(it.id) }

        // Bajar el estado actual del servidor
        val respuesta = remote.listar()
        val delServidor = respuesta.getOrElse { return Result.failure(it) }

        mutex.withLock {
            val recibidos = delServidor.mapNotNull { it.toEntityOrNull() }
            val idsRemotos = recibidos.mapNotNull { it.remoteId }.toSet()

            recibidos.forEach { nuevo ->
                val existente = local.buscarPorRemoteId(nuevo.remoteId!!)
                when {
                    existente == null -> local.insertar(nuevo)
                    existente.sincronizado -> {
                        if (existente.estatus != nuevo.estatus) {
                            notificaciones.crear(existente.usuarioId, existente.id, nuevo.estatus)
                        }
                        local.actualizar(nuevo.copy(id = existente.id))
                    }
                    else -> Unit
                }
            }

            local.obtenerTodos()
                .filter { it.sincronizado && it.remoteId != null && it.remoteId !in idsRemotos }
                .forEach { local.eliminarPorId(it.id) }
        }
        return Result.success(Unit)
    }

    private suspend fun subir(idLocal: Int): Boolean = mutex.withLock {
        val entity = local.buscarPorId(idLocal) ?: return@withLock false
        if (entity.sincronizado) return@withLock true

        val remoteId = entity.remoteId
        val resultado = if (remoteId == null) {
            remote.crear(entity.toDto())
        } else {
            remote.actualizar(remoteId, entity.toDto()).recoverCatching { error ->
                if (error.esNoEncontrado()) remote.crear(entity.copy(remoteId = null).toDto()).getOrThrow()
                else throw error
            }
        }

        resultado.fold(
            onSuccess = { dto ->
                val idServidor = dto.id ?: remoteId ?: return@fold false
                local.marcarSincronizado(idLocal, idServidor)
                true
            },
            onFailure = { false }
        )
    }
}
