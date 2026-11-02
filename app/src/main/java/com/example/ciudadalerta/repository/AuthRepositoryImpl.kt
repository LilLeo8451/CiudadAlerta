package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : AuthRepository {

    // Default: el hash es trabajo de CPU, no debe correr en el hilo principal
    override suspend fun login(correo: String, password: String): Usuario? =
        withContext(Dispatchers.Default) {
            val local = usuarioDao.buscarPorCorreo(correo.trim()) ?: return@withContext null
            if (PasswordHasher.verificar(password, local.passwordHash)) local.toDomain() else null
        }

    override suspend fun registrar(nombre: String, correo: String, password: String): Usuario? =
        withContext(Dispatchers.Default) {
            val correoLimpio = correo.trim().lowercase()
            if (usuarioDao.existeCorreo(correoLimpio)) return@withContext null

            val entity = UsuarioEntity(
                nombre = nombre.trim(),
                correo = correoLimpio,
                passwordHash = PasswordHasher.hashear(password),
                rol = Rol.CIUDADANO
            )
            val idLocal = usuarioDao.insertar(entity).toInt()
            entity.copy(id = idLocal).toDomain()
        }

    override suspend fun existeCorreo(correo: String): Boolean =
        usuarioDao.existeCorreo(correo.trim())

    override suspend fun buscarPorId(id: Int): Usuario? =
        usuarioDao.buscarPorId(id)?.toDomain()
}