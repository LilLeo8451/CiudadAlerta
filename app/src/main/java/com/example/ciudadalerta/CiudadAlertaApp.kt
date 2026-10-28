package com.example.ciudadalerta

import android.app.Application
import com.example.ciudadalerta.di.AppContainer

/** Punto de entrada de la app (registrado en AndroidManifest con android:name). */
class CiudadAlertaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
