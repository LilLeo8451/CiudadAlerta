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
import com.example.ciudadalerta.util.Coordenadas
import com.example.ciudadalerta.util.ProveedorUbicacion
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val repository: ReportRepository,
    private val authRepository: AuthRepository,
    private val multimediaRepository: MultimediaRepository,
    private val proveedorUbicacion: ProveedorUbicacion
) : ViewModel() {

    // Viene de Room, se actualiza solo al crear, editar, borrar o sincronizar
    val reportes: StateFlow<List<Reporte>> = repository.reportes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado del formulario
    val categoria = MutableStateFlow(CategoriaReporte.BACHE)
    val descripcion = MutableStateFlow("")
    val ubicacion = MutableStateFlow<Coordenadas?>(null)
    val hora = MutableStateFlow("12:00")

    // Fotos elegidas en el formulario que todavía no se guardan
    private val _fotosNuevas = MutableStateFlow<List<Uri>>(emptyList())
    val fotosNuevas: StateFlow<List<Uri>> = _fotosNuevas.asStateFlow()

    // Estado de la conexión con la Web API
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
            ubicacionConfirmada = ubicacion.value != null,
            latitud = ubicacion.value?.latitud,
            longitud = ubicacion.value?.longitud,
            fecha = fecha,
            hora = hora.value,
            usuarioId = usuarioId
        )
        val fotos = _fotosNuevas.value
        viewModelScope.launch {
            val reporteId = repository.crear(nuevo)
            fotos.forEach { uri ->
                runCatching { multimediaRepository.agregarFoto(reporteId, uri) }
            }
            // Ahora sí suben las fotos a Cloudinary y se actualiza el reporte en la API
            if (fotos.isNotEmpty()) repository.fotosCambiaron(reporteId)
        }
        descripcion.value = ""
        ubicacion.value = null
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
            // Para que el borrado no se cancele al cerrar el detalle
            withContext(NonCancellable) {
                multimediaRepository.eliminarArchivosDeReporte(id)
                repository.eliminar(id)
            }
        }
    }


    fun crearUriParaCamara(): Uri = multimediaRepository.crearUriParaCamara()

    fun agregarFotoNueva(uri: Uri) {
        _fotosNuevas.update { it + uri }
    }

    fun quitarFotoNueva(uri: Uri) {
        _fotosNuevas.update { it - uri }
    }

    fun fotosDe(reporteId: Int): Flow<List<Multimedia>> =
        multimediaRepository.observarPorReporte(reporteId)

    // Agregar una foto a un reporte que ya existe
    fun agregarFoto(reporteId: Int, uri: Uri) {
        viewModelScope.launch {
            runCatching { multimediaRepository.agregarFoto(reporteId, uri) }
            repository.fotosCambiaron(reporteId)
        }
    }

    fun eliminarFoto(foto: Multimedia) {
        viewModelScope.launch {
            multimediaRepository.eliminar(foto)
            repository.fotosCambiaron(foto.reporteId)
        }
    }

    // GPS
    private val _buscandoUbicacion = MutableStateFlow(false)
    val buscandoUbicacion: StateFlow<Boolean> = _buscandoUbicacion.asStateFlow()

    private val _errorUbicacion = MutableStateFlow(false)
    val errorUbicacion: StateFlow<Boolean> = _errorUbicacion.asStateFlow()

    // Por defecto la guarda en el formulario; editar pasa su propio onResultado
    fun obtenerUbicacion(onResultado: (Coordenadas) -> Unit = { ubicacion.value = it }) {
        if (_buscandoUbicacion.value) return
        viewModelScope.launch {
            _buscandoUbicacion.value = true
            _errorUbicacion.value = false
            val coordenadas = proveedorUbicacion.obtenerActual()
            if (coordenadas == null) _errorUbicacion.value = true else onResultado(coordenadas)
            _buscandoUbicacion.value = false
        }
    }

    // Recuperar desde la Web API
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
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CiudadAlertaApp
                ReportViewModel(
                    app.container.reportRepository,
                    app.container.authRepository,
                    app.container.multimediaRepository,
                    app.container.proveedorUbicacion
                )
            }
        }
    }
}