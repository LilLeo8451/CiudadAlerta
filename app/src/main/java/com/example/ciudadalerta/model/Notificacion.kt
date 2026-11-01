package com.example.ciudadalerta.model

data class Notificacion(
    val id: Int = 0,
    val usuarioId: Int,
    val reporteId: Int,
    val estadoNuevo: EstadoReporte,
    val fecha: Long = System.currentTimeMillis(),
    val leida: Boolean = false
)