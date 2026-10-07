package com.example.ciudadalerta.repository
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ReportRepository {

    private val reportesLocales = mutableListOf(
        Reporte(1, "Bache", "Bache profundo en la calle principal", true, "10:00", "Pendiente"),
        Reporte(2, "Alumbrado", "Lámpara fundida en la esquina", true, "20:30", "Atendido")
    )

    // Función para obtener la lista reactiva
    fun obtenerReportes(): Flow<List<Reporte>> {
        return flowOf(reportesLocales)
    }

    fun guardarReporte(reporte: Reporte) {
        val nuevoId = (reportesLocales.maxOfOrNull { it.id } ?: 0) + 1
        reportesLocales.add(reporte.copy(id = nuevoId))
    }
}