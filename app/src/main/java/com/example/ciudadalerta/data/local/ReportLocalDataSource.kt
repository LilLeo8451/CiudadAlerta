package com.example.ciudadalerta.data.local

import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.model.EstadoReporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Fuente LOCAL de reportes (Room). Es la "fuente de verdad" de la app: la UI siempre
 * observa estos Flow, y la red solo sirve para subir/bajar cambios.
 */
class ReportLocalDataSource(private val dao: ReporteDao) {

    fun observarTodos(): Flow<List<ReporteEntity>> = dao.observarTodos()

    fun observarPorUsuario(usuarioId: Int): Flow<List<ReporteEntity>> = dao.observarPorUsuario(usuarioId)

    fun observarPorId(id: Int): Flow<ReporteEntity?> = dao.observarPorId(id)

    suspend fun obtenerTodos(): List<ReporteEntity> = dao.observarTodos().first()

    suspend fun buscarPorId(id: Int): ReporteEntity? = dao.buscarPorId(id)

    suspend fun buscarPorRemoteId(remoteId: Int): ReporteEntity? = dao.buscarPorRemoteId(remoteId)

    /** Inserta y regresa el id local generado por Room. */
    suspend fun insertar(reporte: ReporteEntity): Int = dao.insertar(reporte).toInt()

    suspend fun actualizar(reporte: ReporteEntity) = dao.actualizar(reporte)

    suspend fun actualizarEstado(id: Int, estado: EstadoReporte) = dao.actualizarEstado(id, estado)

    suspend fun eliminarPorId(id: Int) = dao.eliminarPorId(id)

    suspend fun pendientesDeSincronizar(): List<ReporteEntity> = dao.obtenerPendientesDeSincronizar()

    suspend fun marcarSincronizado(id: Int, remoteId: Int) = dao.marcarSincronizado(id, remoteId)
}
