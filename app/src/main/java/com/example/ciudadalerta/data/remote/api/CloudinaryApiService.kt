package com.example.ciudadalerta.data.remote.api

import com.example.ciudadalerta.data.remote.dto.CloudinaryRespuestaDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Subida de fotos a Cloudinary (servicio de imágenes en la nube)
interface CloudinaryApiService {

    // POST https://api.cloudinary.com/v1_1/{cloudName}/image/upload
    @Multipart
    @POST("{cloudName}/image/upload")
    suspend fun subirImagen(
        @Path("cloudName") cloudName: String,
        @Part archivo: MultipartBody.Part,
        @Part("upload_preset") preset: RequestBody
    ): CloudinaryRespuestaDto
}
