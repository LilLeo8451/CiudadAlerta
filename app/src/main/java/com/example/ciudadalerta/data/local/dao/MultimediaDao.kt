package com.example.ciudadalerta.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MultimediaDao {

    @Insert
    suspend fun insertar(multimedia: MultimediaEntity): Long

    @Delete
    suspend fun eliminar(multimedia: MultimediaEntity)

    @Query("SELECT * FROM multimedia WHERE reporteId = :reporteId")
    fun observarPorReporte(reporteId: Int): Flow<List<MultimediaEntity>>

    @Query("SELECT * FROM multimedia WHERE sincronizado = 0")
    suspend fun obtenerPendientesDeSincronizar(): List<MultimediaEntity>

    @Query("UPDATE multimedia SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Int)
}