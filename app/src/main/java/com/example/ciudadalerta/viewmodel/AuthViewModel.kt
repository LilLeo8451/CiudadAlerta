package com.example.ciudadalerta.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthError {
    CAMPOS_VACIOS,
    CREDENCIALES_INVALIDAS,
    CORREO_INVALIDO,
    CORREO_YA_REGISTRADO,
    PASSWORD_CORTA,
    PASSWORDS_NO_COINCIDEN
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _error = MutableStateFlow<AuthError?>(null)
    val error: StateFlow<AuthError?> = _error.asStateFlow()

    fun limpiarError() {
        _error.value = null
    }

    fun login(correo: String, password: String, onExito: (Usuario) -> Unit) {
        if (correo.isBlank() || password.isBlank()) {
            _error.value = AuthError.CAMPOS_VACIOS
            return
        }
        viewModelScope.launch {
            val usuario = authRepository.login(correo, password)
            if (usuario == null) {
                _error.value = AuthError.CREDENCIALES_INVALIDAS
            } else {
                _error.value = null
                _usuarioActual.value = usuario
                onExito(usuario)
            }
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        password: String,
        confirmar: String,
        onExito: (Usuario) -> Unit
    ) {
        if (nombre.isBlank() || correo.isBlank() || password.isBlank()) {
            _error.value = AuthError.CAMPOS_VACIOS
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            _error.value = AuthError.CORREO_INVALIDO
            return
        }
        if (password.length < 6) {
            _error.value = AuthError.PASSWORD_CORTA
            return
        }
        if (password != confirmar) {
            _error.value = AuthError.PASSWORDS_NO_COINCIDEN
            return
        }
        viewModelScope.launch {
            val usuario = authRepository.registrar(nombre, correo, password)
            if (usuario == null) {
                _error.value = AuthError.CORREO_YA_REGISTRADO
            } else {
                _error.value = null
                _usuarioActual.value = usuario
                onExito(usuario)
            }
        }
    }

    fun logout() {
        _usuarioActual.value = null
        _error.value = null
    }
}