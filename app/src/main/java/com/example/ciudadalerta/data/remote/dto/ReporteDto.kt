package com.example.ciudadalerta.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Forma exacta del JSON que viaja entre la app y la Web API.
 *
 * Ejemplo:
 * {
 *   "id": 7,
 *   "categoria": "BACHE",
 *   "descripcion": "Bache profundo en la calle principal",
 *   "latitud": 19.4326,
 *   "longitud": -99.1332,
 *   "fecha": "27/10/2026",
 *   "hora": "10:00",
 *   "estatus": "PENDIENTE",
 *   "usuarioId": 2
 * }
 *
 * Todos los campos son anulables: Gson no respeta la nulabilidad de Kotlin, así que si el
 * servidor omite un campo no queremos un NullPointerException escondido. La validación se
 * hace en el mapper (ReporteDto.toEntityOrNull()).
 *
 * Las enums viajan como String ("BACHE", "PENDIENTE") para que un valor desconocido del
 * servidor no tumbe la app; se convierten a enum en el mapper.
 */
data class ReporteDto(
    @SerializedName("id") val id: Int? = null,               // lo asigna el servidor; null al crear
    @SerializedName("categoria") val categoria: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("fecha") val fecha: String? = null,      // dd/MM/yyyy
    @SerializedName("hora") val hora: String? = null,        // HH:mm
    @SerializedName("estatus") val estatus: String? = null,
    @SerializedName("usuarioId") val usuarioId: Int? = null
)
