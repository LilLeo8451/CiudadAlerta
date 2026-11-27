package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.data.remote.UsuarioRemoteDataSource
import com.example.ciudadalerta.data.remote.dto.toDto
import com.example.ciudadalerta.data.remote.dto.toEntityOrNull
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val usuarioDao: UsuarioDao,
    private val remote: UsuarioRemoteDataSource
) : AuthRepository {

    override val usuarios: Flow<List<Usuario>> =
        usuarioDao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    // 1) Room (funciona sin internet)  2) si no está o no coincide, se busca en la API
    override suspend fun login(correo: String, password: String): Usuario? {
        val correoLimpio = correo.trim().lowercase()

        val local = usuarioDao.buscarPorCorreo(correoLimpio)
        if (local != null && verificar(password, local.passwordHash)) return local.toDomain()

        val remoto = remote.buscarPorCorreo(correoLimpio).getOrNull()?.toEntityOrNull() ?: return null
        if (!verificar(password, remoto.passwordHash)) return null
        usuarioDao.guardar(remoto)
        return remoto.toDomain()
    }

    // Primero en la API para que el id sea el mismo en todos los teléfonos
    override suspend fun registrar(nombre: String, correo: String, password: String): ResultadoRegistro {
        val correoLimpio = correo.trim().lowercase()
        if (usuarioDao.existeCorreo(correoLimpio)) return ResultadoRegistro.CorreoEnUso

        val existente = remote.buscarPorCorreo(correoLimpio)
            .getOrElse { return ResultadoRegistro.SinConexion }
        if (existente != null) return ResultadoRegistro.CorreoEnUso

        val nuevo = UsuarioEntity(
            nombre = nombre.trim(),
            correo = correoLimpio,
            passwordHash = withContext(Dispatchers.Default) { PasswordHasher.hashear(password) },
            rol = Rol.CIUDADANO
        )
        val creado = remote.crear(nuevo.toDto()).getOrNull()?.toEntityOrNull()
            ?: return ResultadoRegistro.SinConexion
        usuarioDao.guardar(creado)
        return ResultadoRegistro.Exito(creado.toDomain())
    }

    override suspend fun existeCorreo(correo: String): Boolean =
        usuarioDao.existeCorreo(correo.trim())

    override suspend fun buscarPorId(id: Int): Usuario? =
        usuarioDao.buscarPorId(id)?.toDomain()

    override suspend fun sincronizarUsuarios(): Result<Unit> {
        val lista = remote.listar().getOrElse { return Result.failure(it) }
        lista.mapNotNull { it.toEntityOrNull() }.forEach { usuarioDao.guardar(it) }
        return Result.success(Unit)
    }

    // El hash es trabajo de CPU, no debe correr en el hilo principal
    private suspend fun verificar(password: String, hash: String): Boolean =
        withContext(Dispatchers.Default) { PasswordHasher.verificar(password, hash) }
}
