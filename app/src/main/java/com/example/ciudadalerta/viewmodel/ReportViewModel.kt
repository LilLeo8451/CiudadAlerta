package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.ReporteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val reporteRepository: ReporteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val reportes: StateFlow<List<Reporte>> = reporteRepository.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado del formulario
    val categoria = MutableStateFlow(CategoriaReporte.BACHE)
    val descripcion = MutableStateFlow("")
    val ubicacionConfirmada = MutableStateFlow(false)
    val hora = MutableStateFlow("12:00")

    fun observarReporte(id: Int): Flow<Reporte?> = reporteRepository.observarPorId(id)

    fun guardarReporte(usuarioId: Int) {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val reporte = Reporte(
            categoria = categoria.value,
            descripcion = descripcion.value.trim(),
            ubicacionConfirmada = ubicacionConfirmada.value,
            fecha = fecha,
            hora = hora.value,
            usuarioId = usuarioId
        )
        descripcion.value = ""
        ubicacionConfirmada.value = false
        viewModelScope.launch { reporteRepository.crear(reporte) }
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReporte) {
        viewModelScope.launch { reporteRepository.cambiarEstado(id, nuevoEstado) }
    }

    suspend fun nombreAutor(usuarioId: Int): String =
        authRepository.buscarPorId(usuarioId)?.nombre ?: "—"
}