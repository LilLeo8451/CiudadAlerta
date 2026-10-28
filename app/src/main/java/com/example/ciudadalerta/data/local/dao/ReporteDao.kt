package com.example.ciudadalerta.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {

    // ---- Agregar / editar / eliminar ----
    @Insert
    suspend fun insertar(reporte: ReporteEntity): Long

    @Update
    suspend fun actualizar(reporte: ReporteEntity)

    @Delete
    suspend fun eliminar(reporte: ReporteEntity)

    @Query("DELETE FROM reportes WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    // ---- Ver / listar (Flow: la pantalla se actualiza sola cuando cambia la tabla) ----
    @Query("SELECT * FROM reportes ORDER BY id DESC")
    fun observarTodos(): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE usuarioId = :usuarioId ORDER BY id DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE categoria = :categoria ORDER BY id DESC")
    fun observarPorCategoria(categoria: CategoriaReporte): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE id = :id")
    fun observarPorId(id: Int): Flow<ReporteEntity?>

    @Query("SELECT * FROM reportes WHERE id = :id")
    suspend fun buscarPorId(id: Int): ReporteEntity?

    // ---- Cambio de estado (admin): queda marcado para subirse a la API ----
    @Query("UPDATE reportes SET estatus = :estatus, sincronizado = 0 WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estatus: EstadoReporte)

    // ---- Sincronización con la API ----
    @Query("SELECT * FROM reportes WHERE sincronizado = 0")
    suspend fun obtenerPendientesDeSincronizar(): List<ReporteEntity>

    @Query("SELECT * FROM reportes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun buscarPorRemoteId(remoteId: Int): ReporteEntity?

    @Query("UPDATE reportes SET sincronizado = 1, remoteId = :remoteId WHERE id = :id")
    suspend fun marcarSincronizado(id: Int, remoteId: Int)
}