package com.example.ciudadalerta.data.local

import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.local.entity.NotificacionEntity
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.model.Multimedia
import com.example.ciudadalerta.model.Notificacion
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.model.Usuario

fun UsuarioEntity.toModel() = Usuario(id = id, nombre = nombre, correo = correo, rol = rol)

fun ReporteEntity.toModel() = Reporte(
    id = id,
    categoria = categoria,
    descripcion = descripcion,
    ubicacionConfirmada = ubicacionConfirmada,
    fecha = fecha,
    hora = hora,
    estatus = estatus,
    usuarioId = usuarioId,
    latitud = latitud,
    longitud = longitud,
    sincronizado = sincronizado,
    remoteId = remoteId
)

fun Reporte.toEntity() = ReporteEntity(
    id = id,
    categoria = categoria,
    descripcion = descripcion,
    latitud = latitud,
    longitud = longitud,
    ubicacionConfirmada = ubicacionConfirmada,
    fecha = fecha,
    hora = hora,
    estatus = estatus,
    usuarioId = usuarioId,
    sincronizado = sincronizado,
    remoteId = remoteId
)

fun MultimediaEntity.toModel() = Multimedia(id = id, reporteId = reporteId, uri = uri, sincronizado = sincronizado)

fun Multimedia.toEntity() = MultimediaEntity(id = id, reporteId = reporteId, uri = uri, sincronizado = sincronizado)

fun NotificacionEntity.toModel() = Notificacion(
    id = id,
    usuarioId = usuarioId,
    reporteId = reporteId,
    estadoNuevo = estadoNuevo,
    fecha = fecha,
    leida = leida
)