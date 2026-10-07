package com.example.ciudadalerta.model

data class Reporte(
    val id: Int = 0,
    val categoria: String,
    val descripcion: String,
    val ubicacionConfirmada: Boolean,
    val hora: String,
    val estatus: String = "Pendiente"
)