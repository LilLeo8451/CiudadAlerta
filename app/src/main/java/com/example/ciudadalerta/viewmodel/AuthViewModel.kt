package com.example.ciudadalerta.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuthError {
    CAMPOS_VACIOS,
    CREDENCIALES_INVALIDAS,
    CORREO_INVALIDO,
    CORREO_YA_REGISTRADO,
    PASSWORD_CORTA,
    PASSWORDS_NO_COINCIDEN
}

class AuthViewModel : ViewModel() {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _error = MutableStateFlow<AuthError?>(null)
    val error: StateFlow<AuthError?> = _error.asStateFlow()

    fun limpiarError() {
        _error.value = null
    }

    /** Devuelve el usuario si el login fue exitoso, o null y publica el error. */
    fun login(correo: String, password: String): Usuario? {
        if (correo.isBlank() || password.isBlank()) return falla(AuthError.CAMPOS_VACIOS)
        val usuario = UsuarioRepository.login(correo, password)
            ?: return falla(AuthError.CREDENCIALES_INVALIDAS)
        return exito(usuario)
    }

    fun registrar(nombre: String, correo: String, password: String, confirmar: String): Usuario? {
        if (nombre.isBlank() || correo.isBlank() || password.isBlank()) return falla(AuthError.CAMPOS_VACIOS)
        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) return falla(AuthError.CORREO_INVALIDO)
        if (password.length < 6) return falla(AuthError.PASSWORD_CORTA)
        if (password != confirmar) return falla(AuthError.PASSWORDS_NO_COINCIDEN)
        if (UsuarioRepository.existeCorreo(correo)) return falla(AuthError.CORREO_YA_REGISTRADO)
        return exito(UsuarioRepository.registrar(nombre, correo, password))
    }

    fun logout() {
        _usuarioActual.value = null
        _error.value = null
    }

    private fun falla(error: AuthError): Usuario? {
        _error.value = error
        return null
    }

    private fun exito(usuario: Usuario): Usuario {
        _error.value = null
        _usuarioActual.value = usuario
        return usuario
    }
}
