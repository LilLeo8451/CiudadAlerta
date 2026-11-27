package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ciudadalerta.CiudadAlertaApp
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// autor = null si ese usuario no está en este teléfono
data class ReporteConAutor(val reporte: Reporte, val autor: String?)

// Todos los reportes de la ciudad, más recientes primero
class ReportesCiudadViewModel(
    private val repository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // null = todas las categorías
    val filtroCategoria = MutableStateFlow<CategoriaReporte?>(null)

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _sinConexion = MutableStateFlow(false)
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    // Los nombres salen de los usuarios bajados de la API
    val reportes: StateFlow<List<ReporteConAutor>> = combine(
        repository.reportes,
        filtroCategoria,
        authRepository.usuarios
    ) { lista, filtro, usuarios ->
        val nombres = usuarios.associate { it.id to it.nombre }
        val filtrada = if (filtro == null) lista else lista.filter { it.categoria == filtro }
        ordenarMasRecientesPrimero(filtrada).map { ReporteConAutor(it, nombres[it.usuarioId]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        sincronizar()
    }

    fun seleccionarCategoria(categoria: CategoriaReporte?) {
        filtroCategoria.value = categoria
    }

    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            authRepository.sincronizarUsuarios()
            _sinConexion.value = repository.sincronizar().isFailure
            _sincronizando.value = false
        }
    }

    companion object {
        // Por fecha y hora, no por id (los que bajan de la API llegan en cualquier orden)
        fun ordenarMasRecientesPrimero(lista: List<Reporte>): List<Reporte> {
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).apply { isLenient = false }
            fun momento(r: Reporte): Long =
                runCatching { formato.parse("${r.fecha} ${r.hora}")?.time }.getOrNull() ?: 0L
            return lista.sortedWith(
                compareByDescending<Reporte> { momento(it) }.thenByDescending { it.id }
            )
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                ReportesCiudadViewModel(app.container.reportRepository, app.container.authRepository)
            }
        }
    }
}
