package com.example.ciudadalerta.model

enum class Rol { CIUDADANO, ADMIN }

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val rol: Rol = Rol.CIUDADANO
)