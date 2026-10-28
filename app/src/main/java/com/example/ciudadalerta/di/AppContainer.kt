package com.example.ciudadalerta.di

import android.content.Context
import com.example.ciudadalerta.data.local.AppDatabase
import com.example.ciudadalerta.data.local.ReportLocalDataSource
import com.example.ciudadalerta.data.remote.NetworkModule
import com.example.ciudadalerta.data.remote.ReportRemoteDataSource
import com.example.ciudadalerta.repository.ReportRepository
import com.example.ciudadalerta.repository.ReportRepositoryImpl

/**
 * Inyección de dependencias manual. Aquí se arma la cadena:
 *   Application → AppContainer → Repository → (LocalDataSource → Room) + (RemoteDataSource → Retrofit)
 * Los ViewModels piden el repositorio a este contenedor a través de su Factory.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(
            local = ReportLocalDataSource(database.reporteDao()),
            remote = ReportRemoteDataSource(NetworkModule.reporteApi)
        )
    }
}
