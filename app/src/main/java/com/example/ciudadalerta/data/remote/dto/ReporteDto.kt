package com.example.ciudadalerta.data.remote.dto

import com.google.gson.JsonArray
import com.google.gson.JsonElement
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
    @SerializedName("usuarioId") val usuarioId: Int? = null,
    // URLs de Cloudinary. Es JsonElement para que un valor raro (ej. {} de MockAPI) no rompa toda la lista
    @SerializedName("fotos") val fotos: JsonElement? = null
) {
    // Solo las URLs válidas; cualquier otra cosa se ignora
    fun urlsFotos(): List<String> {
        val lista = fotos as? JsonArray ?: return emptyList()
        return lista.mapNotNull { elemento ->
            if (elemento.isJsonPrimitive && elemento.asJsonPrimitive.isString) elemento.asString else null
        }.filter { it.startsWith("http") }
    }
}

fun listaFotosJson(urls: List<String>?): JsonArray? =
    urls?.let { lista -> JsonArray().apply { lista.forEach { url -> add(url) } } }
