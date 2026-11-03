package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ciudadalerta.CiudadAlertaApp
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.Notificacion
import com.example.ciudadalerta.repository.NotificacionRepository
import com.example.ciudadalerta.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Una notificación junto con la categoría de su reporte (para armar el texto). */
data class NotificacionItem(
    val notificacion: Notificacion,
    val categoria: CategoriaReporte?
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotificacionViewModel(
    private val notificacionRepository: NotificacionRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    /** Usuario al que se le muestran los avisos (null = nadie con sesión o administrador). */
    private val usuarioId = MutableStateFlow<Int?>(null)

    val items: StateFlow<List<NotificacionItem>> = usuarioId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                combine(
                    notificacionRepository.observarPorUsuario(id),
                    reportRepository.reportes
                ) { notificaciones, reportes ->
                    notificaciones.map { n ->
                        NotificacionItem(n, reportes.find { it.id == n.reporteId }?.categoria)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val noLeidas: StateFlow<Int> = usuarioId
        .flatMapLatest { id ->
            if (id == null) flowOf(0) else notificacionRepository.observarNoLeidas(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setUsuario(id: Int?) {
        usuarioId.value = id
    }

    fun marcarLeida(id: Int) {
        viewModelScope.launch { notificacionRepository.marcarLeida(id) }
    }

    fun marcarTodasLeidas() {
        val id = usuarioId.value ?: return
        viewModelScope.launch { notificacionRepository.marcarTodasLeidas(id) }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch { notificacionRepository.eliminar(id) }
    }

    companion object {
        /** Uso en Compose: viewModel(factory = NotificacionViewModel.Factory) */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                NotificacionViewModel(
                    app.container.notificacionRepository,
                    app.container.reportRepository
                )
            }
        }
    }
}