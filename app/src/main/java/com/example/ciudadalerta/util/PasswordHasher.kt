package com.example.ciudadalerta.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ALGORITMO = "PBKDF2WithHmacSHA1"   // disponible desde Android API 24
    private const val ITERACIONES = 10_000
    private const val LONGITUD_BITS = 256
    private const val LONGITUD_SALT = 16

    /** Devuelve "salt:hash" en Base64. La sal es aleatoria: dos usuarios con la misma contraseña tienen hashes distintos. */
    fun hashear(password: String): String {
        val salt = ByteArray(LONGITUD_SALT).also { SecureRandom().nextBytes(it) }
        return "${codificar(salt)}:${codificar(derivar(password, salt))}"
    }

    fun verificar(password: String, almacenado: String): Boolean {
        val partes = almacenado.split(":")
        if (partes.size != 2) return false
        val salt = Base64.decode(partes[0], Base64.NO_WRAP)
        val esperado = Base64.decode(partes[1], Base64.NO_WRAP)
        return MessageDigest.isEqual(esperado, derivar(password, salt))
    }

    private fun derivar(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERACIONES, LONGITUD_BITS)
        return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
    }

    private fun codificar(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}