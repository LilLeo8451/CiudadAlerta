package com.example.ciudadalerta.data.remote.dto

import com.google.gson.annotations.SerializedName

// Lo que responde Cloudinary al subir una foto (solo usamos la URL)
data class CloudinaryRespuestaDto(
    @SerializedName("secure_url") val secureUrl: String? = null,
    @SerializedName("public_id") val publicId: String? = null
)
