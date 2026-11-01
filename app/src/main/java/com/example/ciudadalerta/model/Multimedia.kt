package com.example.ciudadalerta.model

data class Multimedia(
    val id: Int = 0,
    val reporteId: Int,
    val uri: String,
    val sincronizado: Boolean = false
)