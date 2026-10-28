package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// TODO: reemplazar por interfaz + ReportLocalDataSource (Room) + ReportRemoteDataSource (Retrofit)
object ReportRepository {

    private val _reportes = MutableStateFlow(
        listOf(
            Reporte(1, CategoriaReporte.BACHE, "Bache profundo en la calle principal", true, "25/10/2026", "10:00", EstadoReporte.PENDIENTE, 2),
            Reporte(2, CategoriaReporte.ALUMBRADO, "Lámpara fundida en la esquina", true, "24/10/2026", "20:30", EstadoReporte.RESUELTO, 2),
            Reporte(3, CategoriaReporte.BASURA, "Basura acumulada junto al mercado", true, "26/10/2026", "08:15", EstadoReporte.EN_REVISION, 2),
            Reporte(4, CategoriaReporte.FUGA_AGUA, "Fuga de agua en la banqueta frente a la escuela", true, "27/10/2026", "07:40", EstadoReporte.PENDIENTE, 2),
            Reporte(5, CategoriaReporte.BACHE, "Bache en el cruce de la avenida", true, "23/10/2026", "17:20", EstadoReporte.PENDIENTE, 2)
        )
    )
    val reportesFlow: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    fun guardarReporte(reporte: Reporte) {
        _reportes.update { lista ->
            val nuevoId = (lista.maxOfOrNull { it.id } ?: 0) + 1
            lista + reporte.copy(id = nuevoId)
        }
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReporte) {
        _reportes.update { lista ->
            lista.map { if (it.id == id) it.copy(estatus = nuevoEstado) else it }
        }
    }
}
