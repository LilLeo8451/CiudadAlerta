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
import com.example.ciudadalerta.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: ReportRepository) : ViewModel() {

    /** null = mostrar todas las categorías */
    val filtroCategoria = MutableStateFlow<CategoriaReporte?>(null)

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _sinConexion = MutableStateFlow(false)
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    /** Pendientes primero, luego en revisión, luego resueltos; dentro de cada grupo, los más nuevos arriba. */
    val reportesFiltrados: StateFlow<List<Reporte>> = combine(
        repository.reportes,
        filtroCategoria
    ) { lista, filtro ->
        val ordenada = lista.sortedWith(
            compareBy<Reporte> { it.estatus.ordinal }.thenByDescending { it.id }
        )
        if (filtro == null) ordenada else ordenada.filter { it.categoria == filtro }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // El admin necesita ver los reportes que mandaron otros ciudadanos desde sus teléfonos
        sincronizar()
    }

    fun seleccionarCategoria(categoria: CategoriaReporte?) {
        filtroCategoria.value = categoria
    }

    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            _sinConexion.value = repository.sincronizar().isFailure
            _sincronizando.value = false
        }
    }

    companion object {
        /** Uso en Compose: viewModel(factory = AdminViewModel.Factory) */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                AdminViewModel(app.container.reportRepository)
            }
        }
    }
}
