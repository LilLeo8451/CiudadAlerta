package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow

class ReportViewModel : ViewModel() {

    val misReportes = ReportRepository.reportesFlow

    var categoria = MutableStateFlow("Bache")
    var descripcion = MutableStateFlow("")
    var ubicacionConfirmada = MutableStateFlow(false)
    var hora = MutableStateFlow("12:00")

    fun guardarReporte() {
        val nuevoReporte = Reporte(
            categoria = categoria.value,
            descripcion = descripcion.value,
            ubicacionConfirmada = ubicacionConfirmada.value,
            hora = hora.value
        )
        ReportRepository.guardarReporte(nuevoReporte)

        descripcion.value = ""
        ubicacionConfirmada.value = false
    }
}