package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que usan los ViewModels. No saben si los datos vienen de Room o de la Web API:
 * eso lo decide la implementación (ReportRepositoryImpl).
 */
interface ReportRepository {

    /** Listar: todos los reportes, se actualiza solo cuando cambia la base de datos. */
    val reportes: Flow<List<Reporte>>

    /** Listar los reportes de un ciudadano. */
    fun observarPorUsuario(usuarioId: Int): Flow<List<Reporte>>

    /** Ver: un reporte por su id local (null si no existe). */
    fun observarPorId(id: Int): Flow<Reporte?>

    /** Agregar. Regresa el id local del reporte creado. */
    suspend fun crear(reporte: Reporte): Int

    /** Editar categoría, descripción, hora, etc. */
    suspend fun actualizar(reporte: Reporte)

    /** Cambio de estatus por parte del administrador. */
    suspend fun cambiarEstado(id: Int, estado: EstadoReporte)

    /** Eliminar. */
    suspend fun eliminar(id: Int)

    /**
     * Recuperar: sube lo pendiente y baja lo que hay en el servidor.
     * Result.failure si no se pudo contactar la Web API (la app sigue funcionando con Room).
     */
    suspend fun sincronizar(): Result<Unit>
}
