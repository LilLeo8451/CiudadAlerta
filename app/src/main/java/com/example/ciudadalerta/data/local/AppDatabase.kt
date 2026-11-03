package com.example.ciudadalerta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ciudadalerta.data.local.dao.MultimediaDao
import com.example.ciudadalerta.data.local.dao.NotificacionDao
import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.local.entity.NotificacionEntity
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ReporteEntity::class,
        MultimediaEntity::class,
        NotificacionEntity::class
    ],
    version = 2,                 // súbela cada vez que cambie una entidad
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun reporteDao(): ReporteDao
    abstract fun multimediaDao(): MultimediaDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ciudad_alerta.db"
                )
                    // Si cambia la versión, recrea la BD (los reportes se vuelven a bajar de la API)
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
    }
}