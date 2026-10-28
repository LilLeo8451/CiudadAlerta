package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte

@Entity(
    tableName = "reportes",
    indices = [Index("usuarioId"), Index("remoteId")]
)
data class ReporteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoria: CategoriaReporte,
    val descripcion: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fecha: String,                         // dd/MM/yyyy
    val hora: String,                          // HH:mm
    val estatus: EstadoReporte = EstadoReporte.PENDIENTE,
    val usuarioId: Int,
    val sincronizado: Boolean = false,         // false = falta subirlo (o subir un cambio) a la API
    val remoteId: Int? = null                  // id que le asigna el servidor
)