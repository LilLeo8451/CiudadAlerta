package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.ReporteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val reporteRepository: ReporteRepository
) : ViewModel() {

    /** null = mostrar todas las categorías */
    val filtroCategoria = MutableStateFlow<CategoriaReporte?>(null)

    /** Pendientes primero, luego en revisión y resueltos; dentro de cada grupo, los más nuevos arriba. */
    val reportesFiltrados: StateFlow<List<Reporte>> = combine(
        reporteRepository.observarTodos(),
        filtroCategoria
    ) { lista, filtro ->
        val ordenada = lista.sortedWith(
            compareBy<Reporte> { it.estatus.ordinal }.thenByDescending { it.id }
        )
        if (filtro == null) ordenada else ordenada.filter { it.categoria == filtro }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refrescar()
    }

    fun seleccionarCategoria(categoria: CategoriaReporte?) {
        filtroCategoria.value = categoria
    }

    /** Sube lo pendiente y baja del servidor los reportes de todos los ciudadanos. */
    fun refrescar() {
        viewModelScope.launch {
            reporteRepository.sincronizarPendientes()
            reporteRepository.refrescarDesdeServidor()
        }
    }
}