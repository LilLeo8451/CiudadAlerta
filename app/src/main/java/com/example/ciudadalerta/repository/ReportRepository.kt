package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.Flow

interface ReportRepository {

    val reportes: Flow<List<Reporte>>

    fun observarPorUsuario(usuarioId: Int): Flow<List<Reporte>>

    fun observarPorId(id: Int): Flow<Reporte?>

    suspend fun crear(reporte: Reporte): Int

    suspend fun actualizar(reporte: Reporte)

    suspend fun cambiarEstado(id: Int, estado: EstadoReporte)

    suspend fun eliminar(id: Int)

    suspend fun sincronizar(): Result<Unit>
}
