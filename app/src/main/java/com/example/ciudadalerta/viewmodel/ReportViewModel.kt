package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.ReportRepository
import com.example.ciudadalerta.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel : ViewModel() {

    val reportes: StateFlow<List<Reporte>> = ReportRepository.reportesFlow

    // Estado del formulario
    val categoria = MutableStateFlow(CategoriaReporte.BACHE)
    val descripcion = MutableStateFlow("")
    val ubicacionConfirmada = MutableStateFlow(false)
    val hora = MutableStateFlow("12:00")

    fun guardarReporte(usuarioId: Int) {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        ReportRepository.guardarReporte(
            Reporte(
                categoria = categoria.value,
                descripcion = descripcion.value.trim(),
                ubicacionConfirmada = ubicacionConfirmada.value,
                fecha = fecha,
                hora = hora.value,
                usuarioId = usuarioId
            )
        )
        descripcion.value = ""
        ubicacionConfirmada.value = false
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReporte) {
        ReportRepository.cambiarEstado(id, nuevoEstado)
    }

    fun nombreAutor(usuarioId: Int): String =
        UsuarioRepository.buscarPorId(usuarioId)?.nombre ?: "—"
}
