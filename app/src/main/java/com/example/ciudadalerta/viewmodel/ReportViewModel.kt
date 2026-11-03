package com.example.ciudadalerta.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ciudadalerta.CiudadAlertaApp
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Multimedia
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.MultimediaRepository
import com.example.ciudadalerta.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val repository: ReportRepository,
    private val authRepository: AuthRepository,
    private val multimediaRepository: MultimediaRepository
) : ViewModel() {

    /** Viene de Room: se actualiza solo al crear, editar, borrar o sincronizar. */
    val reportes: StateFlow<List<Reporte>> = repository.reportes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado del formulario
    val categoria = MutableStateFlow(CategoriaReporte.BACHE)
    val descripcion = MutableStateFlow("")
    val ubicacionConfirmada = MutableStateFlow(false)
    val hora = MutableStateFlow("12:00")

    /** Fotos elegidas en el formulario que todavía no se guardan (el reporte aún no tiene id). */
    private val _fotosNuevas = MutableStateFlow<List<Uri>>(emptyList())
    val fotosNuevas: StateFlow<List<Uri>> = _fotosNuevas.asStateFlow()

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
        val fotos = _fotosNuevas.value
        viewModelScope.launch {
            val reporteId = repository.crear(nuevo)
            fotos.forEach { uri ->
                // Si una foto falla (p. ej. la borraron de la galería) se guardan las demás
                runCatching { multimediaRepository.agregarFoto(reporteId, uri) }
            }
        }
        descripcion.value = ""
        ubicacionConfirmada.value = false
        _fotosNuevas.value = emptyList()
    }

    fun actualizarReporte(reporte: Reporte) {
        viewModelScope.launch { repository.actualizar(reporte) }
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReporte) {
        viewModelScope.launch { repository.cambiarEstado(id, nuevoEstado) }
    }

    fun eliminarReporte(id: Int) {
        viewModelScope.launch {
            multimediaRepository.eliminarArchivosDeReporte(id)
            repository.eliminar(id)
        }
    }

    // ---------- Fotos (multimedia) ----------

    fun crearUriParaCamara(): Uri = multimediaRepository.crearUriParaCamara()

    fun agregarFotoNueva(uri: Uri) {
        _fotosNuevas.update { it + uri }
    }

    fun quitarFotoNueva(uri: Uri) {
        _fotosNuevas.update { it - uri }
    }

    fun fotosDe(reporteId: Int): Flow<List<Multimedia>> =
        multimediaRepository.observarPorReporte(reporteId)

    /** Agregar una foto a un reporte que ya existe (desde el detalle). */
    fun agregarFoto(reporteId: Int, uri: Uri) {
        viewModelScope.launch { runCatching { multimediaRepository.agregarFoto(reporteId, uri) } }
    }

    fun eliminarFoto(foto: Multimedia) {
        viewModelScope.launch { multimediaRepository.eliminar(foto) }
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

    suspend fun nombreAutor(usuarioId: Int): String =
        authRepository.buscarPorId(usuarioId)?.nombre ?: "—"

    companion object {
        /** Uso en Compose: viewModel(factory = ReportViewModel.Factory) */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                ReportViewModel(
                    app.container.reportRepository,
                    app.container.authRepository,
                    app.container.multimediaRepository
                )
            }
        }
    }
}