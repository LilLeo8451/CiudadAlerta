package com.example.ciudadalerta.data.mapper

import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.local.entity.NotificacionEntity
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.model.Multimedia
import com.example.ciudadalerta.model.Notificacion
import com.example.ciudadalerta.model.Usuario

fun UsuarioEntity.toDomain() = Usuario(id = id, nombre = nombre, correo = correo, rol = rol)

fun MultimediaEntity.toDomain() =
    Multimedia(id = id, reporteId = reporteId, uri = uri, sincronizado = sincronizado)

fun Multimedia.toEntity() =
    MultimediaEntity(id = id, reporteId = reporteId, uri = uri, sincronizado = sincronizado)

fun NotificacionEntity.toDomain() = Notificacion(
    id = id,
    usuarioId = usuarioId,
    reporteId = reporteId,
    estadoNuevo = estadoNuevo,
    fecha = fecha,
    leida = leida
)