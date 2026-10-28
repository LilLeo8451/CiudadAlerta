package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ciudadalerta.model.Rol

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: Rol = Rol.CIUDADANO,
    val remoteId: Int? = null      // id que le asigna el servidor
)