package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ciudadalerta.model.EstadoReporte

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = ReporteEntity::class,
            parentColumns = ["id"],
            childColumns = ["reporteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId"), Index("reporteId")]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,                        // a quién va dirigida (el autor del reporte)
    val reporteId: Int,
    val estadoNuevo: EstadoReporte,            // el texto se arma en la UI con strings.xml
    val fecha: Long = System.currentTimeMillis(),
    val leida: Boolean = false
)