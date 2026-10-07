package com.example.ciudadalerta.repository

import com.example.ciudadalerta.model.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object ReportRepository {

    private val reportesLocales = mutableListOf(
        Reporte(1, "Bache", "Bache profundo en la calle principal", true, "10:00", "Pendiente"),
        Reporte(2, "Alumbrado", "Lámpara fundida en la esquina", true, "20:30", "Atendido")
    )

    private val _reportesFlow = MutableStateFlow<List<Reporte>>(reportesLocales.toList())
    val reportesFlow: StateFlow<List<Reporte>> = _reportesFlow

    fun guardarReporte(reporte: Reporte) {
        val nuevoId = (reportesLocales.maxOfOrNull { it.id } ?: 0) + 1
        reportesLocales.add(reporte.copy(id = nuevoId))
        _reportesFlow.value = reportesLocales.toList()
    }
}