package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.Usuario
import kotlinx.coroutines.flow.Flow

sealed interface ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro
    data object CorreoEnUso : ResultadoRegistro
    data object SinConexion : ResultadoRegistro
}

interface AuthRepository {

    // Usuario si las credenciales son correctas, null si no
    suspend fun login(correo: String, password: String): Usuario?

    // Crea la cuenta como CIUDADANO en la API y en Room
    suspend fun registrar(nombre: String, correo: String, password: String): ResultadoRegistro

    suspend fun existeCorreo(correo: String): Boolean

    suspend fun buscarPorId(id: Int): Usuario?

    // Todos los usuarios guardados en el teléfono (para mostrar nombres de autores)
    val usuarios: Flow<List<Usuario>>

    // Baja los usuarios de la API a Room
    suspend fun sincronizarUsuarios(): Result<Unit>
}
