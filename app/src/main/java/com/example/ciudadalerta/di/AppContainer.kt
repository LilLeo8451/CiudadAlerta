package com.example.ciudadalerta.di

import android.content.Context
import com.example.ciudadalerta.data.local.AppDatabase
import com.example.ciudadalerta.data.local.ReportLocalDataSource
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.remote.FotosRemoteDataSource
import com.example.ciudadalerta.data.remote.NetworkModule
import com.example.ciudadalerta.data.remote.ReportRemoteDataSource
import com.example.ciudadalerta.data.remote.UsuarioRemoteDataSource
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.AuthRepositoryImpl
import com.example.ciudadalerta.repository.MultimediaRepository
import com.example.ciudadalerta.repository.MultimediaRepositoryImpl
import com.example.ciudadalerta.repository.NotificacionRepository
import com.example.ciudadalerta.repository.NotificacionRepositoryImpl
import com.example.ciudadalerta.repository.ReportRepository
import com.example.ciudadalerta.repository.ReportRepositoryImpl
import com.example.ciudadalerta.util.AlmacenFotos
import com.example.ciudadalerta.util.PasswordHasher
import com.example.ciudadalerta.util.ProveedorUbicacion

/**
 * Inyección de dependencias manual. Aquí se arma la cadena:
 *   Application → AppContainer → Repository → (Room local) + (Retrofit remoto)
 * Los ViewModels piden sus repositorios a este contenedor a través de su Factory.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            usuarioDao = database.usuarioDao(),
            remote = UsuarioRemoteDataSource(NetworkModule.usuarioApi)
        )
    }

    val notificacionRepository: NotificacionRepository by lazy {
        NotificacionRepositoryImpl(database.notificacionDao())
    }

    private val almacenFotos = AlmacenFotos(context)

    val proveedorUbicacion = ProveedorUbicacion(context)

    val multimediaRepository: MultimediaRepository by lazy {
        MultimediaRepositoryImpl(
            dao = database.multimediaDao(),
            almacen = almacenFotos,
            remote = FotosRemoteDataSource(NetworkModule.cloudinaryApi)
        )
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(
            local = ReportLocalDataSource(database.reporteDao()),
            remote = ReportRemoteDataSource(NetworkModule.reporteApi),
            notificaciones = notificacionRepository,
            fotos = multimediaRepository
        )
    }

    // Admin y ciudadano de prueba con los mismos ids que tienen en MockAPI (1 y 2)
    suspend fun sembrarDatosIniciales() {
        val dao = database.usuarioDao()
        if (!dao.existeCorreo("admin@ciudadalerta.com")) {
            dao.guardar(
                UsuarioEntity(
                    id = 1,
                    remoteId = 1,
                    nombre = "Administrador",
                    correo = "admin@ciudadalerta.com",
                    passwordHash = PasswordHasher.hashear("admin123"),
                    rol = Rol.ADMIN
                )
            )
        }
        if (!dao.existeCorreo("juan@correo.com")) {
            dao.guardar(
                UsuarioEntity(
                    id = 2,
                    remoteId = 2,
                    nombre = "Juan Pérez",
                    correo = "juan@correo.com",
                    passwordHash = PasswordHasher.hashear("123456"),
                    rol = Rol.CIUDADANO
                )
            )
        }
    }
}