package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.api.CloudinaryApiService
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

// Fuente REMOTA de fotos: las sube a Cloudinary y regresa su URL
class FotosRemoteDataSource(private val api: CloudinaryApiService) {

    suspend fun subir(archivo: File): Result<String> =
        try {
            val parte = MultipartBody.Part.createFormData(
                "file",
                archivo.name,
                archivo.asRequestBody("image/jpeg".toMediaType())
            )
            val preset = CloudinaryConfig.UPLOAD_PRESET.toRequestBody("text/plain".toMediaType())
            val respuesta = api.subirImagen(CloudinaryConfig.CLOUD_NAME, parte, preset)
            val url = respuesta.secureUrl ?: throw IOException("Cloudinary no regresó la URL")
            Result.success(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
