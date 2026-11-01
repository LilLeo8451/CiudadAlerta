package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.NotificacionDao
import com.example.ciudadalerta.data.local.entity.NotificacionEntity
import com.example.ciudadalerta.data.local.toModel
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Notificacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificacionRepositoryImpl(
    private val dao: NotificacionDao
) : NotificacionRepository {

    override fun observarPorUsuario(usuarioId: Int): Flow<List<Notificacion>> =
        dao.observarPorUsuario(usuarioId).map { lista -> lista.map { it.toModel() } }

    override fun observarNoLeidas(usuarioId: Int): Flow<Int> = dao.observarNoLeidas(usuarioId)

    override suspend fun crear(usuarioId: Int, reporteId: Int, estadoNuevo: EstadoReporte) {
        dao.insertar(
            NotificacionEntity(usuarioId = usuarioId, reporteId = reporteId, estadoNuevo = estadoNuevo)
        )
    }

    override suspend fun marcarLeida(id: Int) = dao.marcarLeida(id)

    override suspend fun marcarTodasLeidas(usuarioId: Int) = dao.marcarTodasLeidas(usuarioId)

    override suspend fun eliminar(id: Int) = dao.eliminar(id)
}