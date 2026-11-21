package com.rinconsur.app.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rinconsur.app.repository.models.Acceso
import com.rinconsur.app.repository.models.Parametros
import com.rinconsur.app.repository.models.Registro
import com.rinconsur.app.repository.models.User

@Database(
    entities = [User::class, Acceso::class, Parametros::class, Registro::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accesoDao(): AccesoDao
    abstract fun parametrosDao(): ParametrosDao
    abstract fun registroDao(): RegistroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rinconsur_database"
                )
                    // Proyecto académico: al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos iniciales. Se ejecuta en cada apertura con INSERT OR IGNORE,
        // así no se duplican y también se cargan después de una migración destructiva.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO accesos (id, nombre) VALUES " +
                        "(1, 'Acceso 1 - Principal'), (2, 'Acceso 2 - Estacionamiento'), " +
                        "(3, 'Acceso 3 - Calle lateral'), (4, 'Acceso 4 - Patio de comidas')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO users (name, email, password) " +
                        "VALUES ('Encargado Demo', 'encargado@rinconsur.cl', 'rincon123')"
                )
            }
        }
    }
}
