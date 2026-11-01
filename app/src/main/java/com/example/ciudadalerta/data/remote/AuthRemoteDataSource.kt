package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.model.Usuario

interface AuthRemoteDataSource {

    /** POST /login */
    suspend fun login(correo: String, password: String): Result<Usuario>

    /** POST /usuarios */
    suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario>
}