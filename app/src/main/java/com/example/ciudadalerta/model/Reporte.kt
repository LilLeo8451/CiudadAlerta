package com.example.ciudadalerta.model

enum class CategoriaReporte { BACHE, ALUMBRADO, BASURA, FUGA_AGUA }

enum class EstadoReporte { PENDIENTE, EN_REVISION, RESUELTO }

data class Reporte(
    val id: Int = 0,
    val categoria: CategoriaReporte,
    val descripcion: String,
    val ubicacionConfirmada: Boolean,
    val fecha: String,          // dd/MM/yyyy
    val hora: String,           // HH:mm
    val estatus: EstadoReporte = EstadoReporte.PENDIENTE,
    val usuarioId: Int,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val sincronizado: Boolean = false,   // false = falta subirlo a la API
    val remoteId: Int? = null            // id que le asigna el servidor
)