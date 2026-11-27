package com.example.ciudadalerta.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // Lanza excepción si el correo ya existe: revisa con existeCorreo() antes de insertar
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE correo = :correo COLLATE NOCASE)")
    suspend fun existeCorreo(correo: String): Boolean

    // Copia local de un usuario que viene de la API (reemplaza si ya estaba)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios")
    fun observarTodos(): Flow<List<UsuarioEntity>>
}