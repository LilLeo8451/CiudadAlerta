package com.example.ciudadalerta.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ciudadalerta.CiudadAlertaApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AuthViewModel(app().container.authRepository) }
        initializer {
            ReportViewModel(app().container.reporteRepository, app().container.authRepository)
        }
        initializer { AdminViewModel(app().container.reporteRepository) }
    }
}

private fun CreationExtras.app(): CiudadAlertaApp = this[APPLICATION_KEY] as CiudadAlertaApp