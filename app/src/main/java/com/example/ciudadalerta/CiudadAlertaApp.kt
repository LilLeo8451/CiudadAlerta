package com.example.ciudadalerta

import android.app.Application
import com.example.ciudadalerta.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Punto de entrada de la app (registrado en AndroidManifest con android:name). */
class CiudadAlertaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.sembrarDatosIniciales()
        }
    }
}