package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.model.Usuario

// TODO: reemplazar la lista en memoria por Room (local) + Retrofit (remoto)
object UsuarioRepository {

    private val usuarios = mutableListOf(
        Usuario(1, "Administrador", "admin@ciudadalerta.com", "admin123", Rol.ADMIN),
        Usuario(2, "Juan Pérez", "juan@correo.com", "123456", Rol.CIUDADANO)
    )

    fun login(correo: String, password: String): Usuario? =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) && it.password == password }

    fun existeCorreo(correo: String): Boolean =
        usuarios.any { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun registrar(nombre: String, correo: String, password: String): Usuario {
        val nuevo = Usuario(
            id = (usuarios.maxOfOrNull { it.id } ?: 0) + 1,
            nombre = nombre.trim(),
            correo = correo.trim(),
            password = password,
            rol = Rol.CIUDADANO
        )
        usuarios.add(nuevo)
        return nuevo
    }

    fun buscarPorId(id: Int): Usuario? = usuarios.find { it.id == id }
}
