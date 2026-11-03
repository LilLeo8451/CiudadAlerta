package com.example.ciudadalerta.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Maneja los archivos de las fotos de los reportes.
 *
 * - Las fotos definitivas se copian a filesDir/fotos (almacenamiento privado de la app),
 *   así siguen disponibles aunque el usuario borre la original de su galería.
 * - Las fotos de la cámara se toman primero en cacheDir/camara (temporal) a través del FileProvider.
 * - En Room solo se guarda la ruta ("file:///.../fotos/xxxx.jpg") en MultimediaEntity.uri.
 */
class AlmacenFotos(context: Context) {

    private val appContext = context.applicationContext
    private val autoridad = "${appContext.packageName}.fileprovider"

    private val carpetaFotos: File
        get() = File(appContext.filesDir, "fotos").apply { mkdirs() }

    private val carpetaCamara: File
        get() = File(appContext.cacheDir, "camara").apply { mkdirs() }

    /** Uri temporal donde la app de cámara va a escribir la foto. */
    fun crearUriParaCamara(): Uri {
        val archivo = File(carpetaCamara, "foto_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(appContext, autoridad, archivo)
    }

    /** Copia la imagen (de la galería o de la cámara) a la carpeta de fotos y regresa su ruta. */
    suspend fun guardar(origen: Uri): String = withContext(Dispatchers.IO) {
        val destino = File(carpetaFotos, "${UUID.randomUUID()}.jpg")
        val entrada = appContext.contentResolver.openInputStream(origen)
            ?: throw IOException("No se pudo abrir la imagen: $origen")
        entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }

        // Si venía de la cámara, ya no necesitamos el temporal
        if (origen.authority == autoridad) {
            origen.lastPathSegment?.let { File(carpetaCamara, it).delete() }
        }
        Uri.fromFile(destino).toString()
    }

    /** Borra el archivo de una foto guardada (solo si está dentro de la carpeta de fotos). */
    suspend fun eliminar(uri: String) {
        withContext<Unit>(Dispatchers.IO) {
            val ruta = Uri.parse(uri).path ?: return@withContext
            val archivo = File(ruta)
            if (archivo.canonicalPath.startsWith(carpetaFotos.canonicalPath)) archivo.delete()
        }
    }
}
