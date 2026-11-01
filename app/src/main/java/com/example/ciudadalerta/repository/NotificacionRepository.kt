package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Notificacion
import kotlinx.coroutines.flow.Flow

interface NotificacionRepository {

    fun observarPorUsuario(usuarioId: Int): Flow<List<Notificacion>>
    fun observarNoLeidas(usuarioId: Int): Flow<Int>

    suspend fun crear(usuarioId: Int, reporteId: Int, estadoNuevo: EstadoReporte)
    suspend fun marcarLeida(id: Int)
    suspend fun marcarTodasLeidas(usuarioId: Int)
    suspend fun eliminar(id: Int)
}