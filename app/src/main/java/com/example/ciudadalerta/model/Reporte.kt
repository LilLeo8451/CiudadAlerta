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
    val usuarioId: Int
)
