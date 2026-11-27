package com.example.ciudadalerta.data.remote.dto

import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.model.Rol
import com.google.gson.annotations.SerializedName

// JSON del recurso "usuarios" en MockAPI
data class UsuarioDto(
    @SerializedName("id") val id: Int? = null,               // lo asigna el servidor
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("passwordHash") val passwordHash: String? = null,   // nunca la contraseña en texto
    @SerializedName("rol") val rol: String? = null           // CIUDADANO | ADMIN
)

// El id local es el mismo que el del servidor, así los reportes apuntan al mismo usuario en todos los teléfonos
fun UsuarioDto.toEntityOrNull(): UsuarioEntity? {
    val idServidor = id ?: return null
    val correoValido = correo?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: return null
    val hash = passwordHash?.takeIf { it.contains(":") } ?: return null
    return UsuarioEntity(
        id = idServidor,
        nombre = nombre.orEmpty(),
        correo = correoValido,
        passwordHash = hash,
        rol = Rol.entries.firstOrNull { it.name.equals(rol, ignoreCase = true) } ?: Rol.CIUDADANO,
        remoteId = idServidor
    )
}

fun UsuarioEntity.toDto(): UsuarioDto =
    UsuarioDto(nombre = nombre, correo = correo, passwordHash = passwordHash, rol = rol.name)
