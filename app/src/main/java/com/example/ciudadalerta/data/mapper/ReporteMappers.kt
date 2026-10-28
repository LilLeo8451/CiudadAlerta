package com.example.ciudadalerta.data.mapper

import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.remote.dto.ReporteDto
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte

/*
 * Conversión entre las 3 representaciones de un reporte:
 *   Reporte        → modelo que usan la UI y los ViewModels
 *   ReporteEntity  → fila de Room (id local + sincronizado + remoteId)
 *   ReporteDto     → JSON de la Web API (su "id" es el remoteId)
 */

// ---------- Room ↔ dominio ----------

fun ReporteEntity.toDomain(): Reporte = Reporte(
    id = id,
    categoria = categoria,
    descripcion = descripcion,
    ubicacionConfirmada = latitud != null && longitud != null,
    fecha = fecha,
    hora = hora,
    estatus = estatus,
    usuarioId = usuarioId
)

fun Reporte.toEntity(
    latitud: Double? = null,
    longitud: Double? = null,
    remoteId: Int? = null
): ReporteEntity = ReporteEntity(
    id = id,
    categoria = categoria,
    descripcion = descripcion,
    latitud = latitud,
    longitud = longitud,
    fecha = fecha,
    hora = hora,
    estatus = estatus,
    usuarioId = usuarioId,
    sincronizado = false,       // cualquier cambio hecho en la app queda pendiente de subir
    remoteId = remoteId
)

// ---------- Room ↔ Web API ----------

fun ReporteEntity.toDto(): ReporteDto = ReporteDto(
    id = remoteId,
    categoria = categoria.name,
    descripcion = descripcion,
    latitud = latitud,
    longitud = longitud,
    fecha = fecha,
    hora = hora,
    estatus = estatus.name,
    usuarioId = usuarioId
)

/**
 * Convierte lo que llegó del servidor a una fila de Room ya sincronizada.
 * Regresa null si el JSON viene incompleto o con una categoría desconocida (se ignora ese registro).
 */
fun ReporteDto.toEntityOrNull(localId: Int = 0): ReporteEntity? {
    val remoteId = id ?: return null
    val categoria = enumOrNull<CategoriaReporte>(categoria) ?: return null
    return ReporteEntity(
        id = localId,
        categoria = categoria,
        descripcion = descripcion.orEmpty(),
        latitud = latitud,
        longitud = longitud,
        fecha = fecha.orEmpty(),
        hora = hora.orEmpty(),
        estatus = enumOrNull<EstadoReporte>(estatus) ?: EstadoReporte.PENDIENTE,
        usuarioId = usuarioId ?: 0,
        sincronizado = true,
        remoteId = remoteId
    )
}

private inline fun <reified T : Enum<T>> enumOrNull(nombre: String?): T? =
    enumValues<T>().firstOrNull { it.name.equals(nombre?.trim(), ignoreCase = true) }
