package com.example.ciudadalerta.model

enum class Rol { CIUDADANO, ADMIN }

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,   // TODO: guardar el hash cuando se pase a Room/API, nunca en texto plano
    val rol: Rol = Rol.CIUDADANO
)
