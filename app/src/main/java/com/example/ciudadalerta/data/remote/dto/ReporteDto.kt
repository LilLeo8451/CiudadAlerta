package com.example.ciudadalerta.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReporteDto(
    @SerializedName("id") val id: Int? = null,               // lo asigna el servidor; null al crear
    @SerializedName("categoria") val categoria: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("ubicacionConfirmada") val ubicacionConfirmada: Boolean? = null,
    @SerializedName("fecha") val fecha: String? = null,      // dd/MM/yyyy
    @SerializedName("hora") val hora: String? = null,        // HH:mm
    @SerializedName("estatus") val estatus: String? = null,
    @SerializedName("usuarioId") val usuarioId: Int? = null
)
