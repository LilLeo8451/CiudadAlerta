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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repositorio "offline-first":
 *  1. Todo cambio se guarda primero en Room (sincronizado = false) → la UI se actualiza al instante.
 *  2. Luego se intenta mandar a la Web API. Si responde, se marca sincronizado = true y se guarda el remoteId.
 *  3. Si no hay internet, el reporte se queda pendiente y se vuelve a intentar en sincronizar().
 */
class ReportRepositoryImpl(
    private val local: ReportLocalDataSource,
    private val remote: ReportRemoteDataSource
) : ReportRepository {

    /** Evita que dos sincronizaciones al mismo tiempo suban dos veces el mismo reporte. */
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
        // Conservamos lo que el modelo de dominio no trae (coordenadas y remoteId)
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
        local.actualizarEstado(id, estado)   // el DAO ya lo marca sincronizado = 0
        subir(id)
    }

    override suspend fun eliminar(id: Int) {
        val reporte = local.buscarPorId(id) ?: return
        local.eliminarPorId(id)
        // Si nunca llegó al servidor no hay nada que borrar allá.
        // Nota: si falla por falta de internet, el registro sigue en el servidor y volverá
        // a bajar en la próxima sincronización.
        reporte.remoteId?.let { remote.eliminar(it) }
    }

    override suspend fun sincronizar(): Result<Unit> {
        // 1) Subir todo lo que se creó/editó sin conexión
        local.pendientesDeSincronizar().forEach { subir(it.id) }

        // 2) Bajar el estado actual del servidor
        val respuesta = remote.listar()
        val delServidor = respuesta.getOrElse { return Result.failure(it) }

        mutex.withLock {
            val recibidos = delServidor.mapNotNull { it.toEntityOrNull() }
            val idsRemotos = recibidos.mapNotNull { it.remoteId }.toSet()

            recibidos.forEach { nuevo ->
                val existente = local.buscarPorRemoteId(nuevo.remoteId!!)
                when {
                    existente == null -> local.insertar(nuevo)
                    // Solo sobrescribimos si no hay cambios locales pendientes de subir
                    existente.sincronizado -> local.actualizar(nuevo.copy(id = existente.id))
                    else -> Unit
                }
            }

            // Lo que ya estaba sincronizado pero desapareció del servidor, se borró allá
            local.obtenerTodos()
                .filter { it.sincronizado && it.remoteId != null && it.remoteId !in idsRemotos }
                .forEach { local.eliminarPorId(it.id) }
        }
        return Result.success(Unit)
    }

    /**
     * Manda un reporte local a la Web API: POST si es nuevo, PUT si ya tenía remoteId.
     * Regresa true si quedó sincronizado.
     */
    private suspend fun subir(idLocal: Int): Boolean = mutex.withLock {
        val entity = local.buscarPorId(idLocal) ?: return@withLock false
        if (entity.sincronizado) return@withLock true

        val remoteId = entity.remoteId
        val resultado = if (remoteId == null) {
            remote.crear(entity.toDto())
        } else {
            remote.actualizar(remoteId, entity.toDto()).recoverCatching { error ->
                // El servidor ya no lo tiene (p. ej. se reinició la API): lo creamos de nuevo
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
