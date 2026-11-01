package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.Usuario

interface AuthRepository {

    /** Devuelve el usuario si las credenciales son correctas, o null si no. */
    suspend fun login(correo: String, password: String): Usuario?

    /** Crea la cuenta como CIUDADANO. Devuelve null si el correo ya existe. */
    suspend fun registrar(nombre: String, correo: String, password: String): Usuario?

    suspend fun existeCorreo(correo: String): Boolean

    suspend fun buscarPorId(id: Int): Usuario?
}