package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "multimedia",
    foreignKeys = [
        ForeignKey(
            entity = ReporteEntity::class,
            parentColumns = ["id"],
            childColumns = ["reporteId"],
            onDelete = ForeignKey.CASCADE      // si se borra el reporte, se borran sus fotos
        )
    ],
    indices = [Index("reporteId")]
)
data class MultimediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporteId: Int,
    val uri: String,                           // ruta o URI de la foto
    val sincronizado: Boolean = false
)