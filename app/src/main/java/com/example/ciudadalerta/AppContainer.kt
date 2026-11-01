package com.example.ciudadalerta

import android.content.Context
import com.example.ciudadalerta.data.local.AppDatabase
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.data.remote.AuthRemoteDataSource
import com.example.ciudadalerta.data.remote.FakeAuthRemoteDataSource
import com.example.ciudadalerta.data.remote.FakeReporteRemoteDataSource
import com.example.ciudadalerta.data.remote.ReporteRemoteDataSource
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.repository.AuthRepository
import com.example.ciudadalerta.repository.AuthRepositoryImpl
import com.example.ciudadalerta.repository.MultimediaRepository
import com.example.ciudadalerta.repository.MultimediaRepositoryImpl
import com.example.ciudadalerta.repository.NotificacionRepository
import com.example.ciudadalerta.repository.NotificacionRepositoryImpl
import com.example.ciudadalerta.repository.ReporteRepository
import com.example.ciudadalerta.repository.ReporteRepositoryImpl
import com.example.ciudadalerta.util.PasswordHasher

class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    // TODO (compañero): cambiar estas 2 líneas por las implementaciones con Retrofit
    private val reporteRemote: ReporteRemoteDataSource = FakeReporteRemoteDataSource()
    private val authRemote: AuthRemoteDataSource = FakeAuthRemoteDataSource()

    val authRepository: AuthRepository =
        AuthRepositoryImpl(database.usuarioDao(), authRemote)

    val reporteRepository: ReporteRepository =
        ReporteRepositoryImpl(database.reporteDao(), database.notificacionDao(), reporteRemote)

    val multimediaRepository: MultimediaRepository =
        MultimediaRepositoryImpl(database.multimediaDao(), database.reporteDao(), reporteRemote)

    val notificacionRepository: NotificacionRepository =
        NotificacionRepositoryImpl(database.notificacionDao())

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