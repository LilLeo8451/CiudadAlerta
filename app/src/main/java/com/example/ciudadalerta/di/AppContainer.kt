package com.example.ciudadalerta.di

import android.content.Context
import com.example.ciudadalerta.data.local.AppDatabase
import com.example.ciudadalerta.data.local.ReportLocalDataSource
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.remote.NetworkModule
import com.example.ciudadalerta.data.remote.ReportRemoteDataSource
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.AuthRepositoryImpl
import com.example.ciudadalerta.repository.MultimediaRepository
import com.example.ciudadalerta.repository.MultimediaRepositoryImpl
import com.example.ciudadalerta.repository.NotificacionRepository
import com.example.ciudadalerta.repository.NotificacionRepositoryImpl
import com.example.ciudadalerta.repository.ReportRepository
import com.example.ciudadalerta.repository.ReportRepositoryImpl
import com.example.ciudadalerta.util.PasswordHasher

/**
 * Inyección de dependencias manual. Aquí se arma la cadena:
 *   Application → AppContainer → Repository → (Room local) + (Retrofit remoto)
 * Los ViewModels piden sus repositorios a este contenedor a través de su Factory.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.usuarioDao())
    }

    val notificacionRepository: NotificacionRepository by lazy {
        NotificacionRepositoryImpl(database.notificacionDao())
    }

    val multimediaRepository: MultimediaRepository by lazy {
        MultimediaRepositoryImpl(database.multimediaDao())
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(
            local = ReportLocalDataSource(database.reporteDao()),
            remote = ReportRemoteDataSource(NetworkModule.reporteApi),
            notificaciones = notificacionRepository
        )
    }

    /** Crea el administrador y un ciudadano de prueba la primera vez que se abre la app. */
    suspend fun sembrarDatosIniciales() {
        val dao = database.usuarioDao()
        if (!dao.existeCorreo("admin@ciudadalerta.com")) {
            dao.insertar(
                UsuarioEntity(
                    nombre = "Administrador",
                    correo = "admin@ciudadalerta.com",
                    passwordHash = PasswordHasher.hashear("admin123"),
                    rol = Rol.ADMIN
                )
            )
        }
        if (!dao.existeCorreo("juan@correo.com")) {
            dao.insertar(
                UsuarioEntity(
                    nombre = "Juan Pérez",
                    correo = "juan@correo.com",
                    passwordHash = PasswordHasher.hashear("123456"),
                    rol = Rol.CIUDADANO
                )
            )
        }
    }
}