package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.Flow

interface ReporteRepository {

    // ---- Lecturas: siempre salen de Room. La pantalla se actualiza sola ----
    fun observarTodos(): Flow<List<Reporte>>
    fun observarPorUsuario(usuarioId: Int): Flow<List<Reporte>>
    fun observarPorId(id: Int): Flow<Reporte?>

    // ---- Escrituras: primero se guarda en Room (sincronizado = false) y luego se intenta subir ----
    /** Devuelve el id local del reporte creado. */
    suspend fun crear(reporte: Reporte): Int
    suspend fun editar(reporte: Reporte)
    suspend fun cambiarEstado(id: Int, estado: EstadoReporte)
    suspend fun eliminar(id: Int)

    // ---- Sincronización con la API ----
    /** "Recuperar": baja los reportes del servidor y los guarda en Room. usuarioId = null trae todos (admin). */
    suspend fun refrescarDesdeServidor(usuarioId: Int? = null): Result<Unit>

    /** Sube lo que quedó pendiente (sincronizado = false), por ejemplo, lo creado sin internet. */
    suspend fun sincronizarPendientes(): Result<Unit>
}