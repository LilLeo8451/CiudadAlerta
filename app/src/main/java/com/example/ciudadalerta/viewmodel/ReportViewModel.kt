package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ciudadalerta.CiudadAlertaApp
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.ReportRepository
import com.example.ciudadalerta.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    /** Viene de Room: se actualiza solo al crear, editar, borrar o sincronizar. */
    val reportes: StateFlow<List<Reporte>> = repository.reportes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado del formulario
    val categoria = MutableStateFlow(CategoriaReporte.BACHE)
    val descripcion = MutableStateFlow("")
    val ubicacionConfirmada = MutableStateFlow(false)
    val hora = MutableStateFlow("12:00")

    // Estado de la conexión con la Web API (para mostrar un indicador o un mensaje en la UI)
    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _sinConexion = MutableStateFlow(false)
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    init {
        sincronizar()
    }

    fun guardarReporte(usuarioId: Int) {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val nuevo = Reporte(
            categoria = categoria.value,
            descripcion = descripcion.value.trim(),
            ubicacionConfirmada = ubicacionConfirmada.value,
            fecha = fecha,
            hora = hora.value,
            usuarioId = usuarioId
        )
        viewModelScope.launch { repository.crear(nuevo) }
        descripcion.value = ""
        ubicacionConfirmada.value = false
    }

    fun actualizarReporte(reporte: Reporte) {
        viewModelScope.launch { repository.actualizar(reporte) }
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReporte) {
        viewModelScope.launch { repository.cambiarEstado(id, nuevoEstado) }
    }

    fun eliminarReporte(id: Int) {
        viewModelScope.launch { repository.eliminar(id) }
    }

    /** Recuperar desde la Web API (también se llama sola al abrir la pantalla). */
    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            _sinConexion.value = repository.sincronizar().isFailure
            _sincronizando.value = false
        }
    }

    // TODO: pasar a UsuarioRepository con Room cuando esté listo
    fun nombreAutor(usuarioId: Int): String =
        UsuarioRepository.buscarPorId(usuarioId)?.nombre ?: "—"

    companion object {
        /** Uso en Compose: viewModel(factory = ReportViewModel.Factory) */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                ReportViewModel(app.container.reportRepository)
            }
        }
    }
}
