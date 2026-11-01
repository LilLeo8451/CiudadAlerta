package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.local.toModel
import com.example.ciudadalerta.data.remote.AuthRemoteDataSource
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val usuarioDao: UsuarioDao,
    private val remote: AuthRemoteDataSource
) : AuthRepository {

    // Default: el hash de la contraseña es trabajo de CPU, no debe correr en el hilo principal
    override suspend fun login(correo: String, password: String): Usuario? = withContext(Dispatchers.Default) {
        val correoLimpio = correo.trim()
        val local = usuarioDao.buscarPorCorreo(correoLimpio)
        if (local != null) {
            return@withContext if (PasswordHasher.verificar(password, local.passwordHash)) local.toModel() else null
        }

        // No existe en este celular: se intenta con el servidor y se guarda una copia local
        val remoto = remote.login(correoLimpio, password).getOrNull() ?: return@withContext null
        val copia = UsuarioEntity(
            id = remoto.id,                       // el id local coincide con el del servidor
            nombre = remoto.nombre,
            correo = remoto.correo.lowercase(),
            passwordHash = PasswordHasher.hashear(password),
            rol = remoto.rol,
            remoteId = remoto.id
        )
        runCatching { usuarioDao.insertar(copia) }
        copia.toModel()
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

            // Intento de alta en el servidor (si falla, la cuenta queda solo en este celular)
            // OJO: aquí hay que acordar con el compañero cómo se alinean los ids local/servidor
            remote.registrar(entity.nombre, correoLimpio, password).onSuccess { remoto ->
                usuarioDao.actualizar(entity.copy(id = idLocal, remoteId = remoto.id))
            }

            entity.copy(id = idLocal).toModel()
        }

    override suspend fun existeCorreo(correo: String): Boolean = usuarioDao.existeCorreo(correo.trim())

    override suspend fun buscarPorId(id: Int): Usuario? = usuarioDao.buscarPorId(id)?.toModel()
}