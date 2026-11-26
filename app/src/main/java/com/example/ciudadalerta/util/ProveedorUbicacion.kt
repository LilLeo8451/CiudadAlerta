package com.example.ciudadalerta.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class Coordenadas(val latitud: Double, val longitud: Double)

fun formatearCoordenadas(latitud: Double, longitud: Double): String =
    String.format(Locale.US, "%.5f, %.5f", latitud, longitud)

// Lee la ubicación actual del GPS con FusedLocationProvider de Google
class ProveedorUbicacion(context: Context) {

    private val appContext = context.applicationContext
    private val cliente = LocationServices.getFusedLocationProviderClient(appContext)

    fun tienePermiso(): Boolean = tienePermisoUbicacion(appContext)

    // null si no hay permiso o no se pudo obtener
    @SuppressLint("MissingPermission")
    suspend fun obtenerActual(): Coordenadas? {
        if (!tienePermiso()) return null
        val token = CancellationTokenSource()
        val ubicacion = cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
            .esperar { token.cancel() }
            ?: cliente.lastLocation.esperar()
        return ubicacion?.let { Coordenadas(it.latitude, it.longitude) }
    }
}

fun tienePermisoUbicacion(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

// Convierte la Task de Google en una función suspend
private suspend fun <T> Task<T>.esperar(alCancelar: () -> Unit = {}): T? =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { if (cont.isActive) cont.resume(it) }
        addOnFailureListener { if (cont.isActive) cont.resume(null) }
        addOnCanceledListener { if (cont.isActive) cont.resume(null) }
        cont.invokeOnCancellation { alCancelar() }
    }
