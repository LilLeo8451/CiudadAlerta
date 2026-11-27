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
    private val notificaciones: NotificacionRepository,
    private val fotos: MultimediaRepository
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
                latitud = reporte.latitud ?: actual.latitud,
                longitud = reporte.longitud ?: actual.longitud,
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

    override suspend fun fotosCambiaron(id: Int) {
        local.marcarPendiente(id)
        subir(id)
    }

    override suspend fun sincronizar(): Result<Unit> {
        local.pendientesDeSincronizar().forEach { subir(it.id) }

        // Bajar el estado actual del servidor
        val respuesta = remote.listar()
        val delServidor = respuesta.getOrElse { return Result.failure(it) }

        mutex.withLock {
            // Cada reporte junto con las URLs de sus fotos
            val recibidos = delServidor.mapNotNull { dto ->
                dto.toEntityOrNull()?.let { it to dto.urlsFotos() }
            }
            val idsRemotos = recibidos.mapNotNull { it.first.remoteId }.toSet()

            recibidos.forEach { (nuevo, urlsFotos) ->
                val existente = local.buscarPorRemoteId(nuevo.remoteId!!)
                when {
                    existente == null -> {
                        val idLocal = local.insertar(nuevo)
                        fotos.aplicarFotosDelServidor(idLocal, urlsFotos)
                    }
                    existente.sincronizado -> {
                        if (existente.estatus != nuevo.estatus) {
                            notificaciones.crear(existente.usuarioId, existente.id, nuevo.estatus)
                        }
                        local.actualizar(nuevo.copy(id = existente.id))
                        fotos.aplicarFotosDelServidor(existente.id, urlsFotos)
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

        // Primero las fotos a Cloudinary; sus URLs viajan dentro del reporte
        val urlsFotos = fotos.subirPendientes(idLocal).getOrElse { return@withLock false }

        val remoteId = entity.remoteId
        val resultado = if (remoteId == null) {
            remote.crear(entity.toDto(urlsFotos))
        } else {
            remote.actualizar(remoteId, entity.toDto(urlsFotos)).recoverCatching { error ->
                if (error.esNoEncontrado()) remote.crear(entity.copy(remoteId = null).toDto(urlsFotos)).getOrThrow()
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
