package com.rinconsur.app.repository.data

import androidx.room.Dao
import androidx.room.Query
import com.rinconsur.app.repository.models.Acceso
import kotlinx.coroutines.flow.Flow

@Dao
interface AccesoDao {
    // READ: los 4 accesos de la tienda (se cargan en AppDatabase.seedCallback)
    @Query("SELECT * FROM accesos ORDER BY id ASC")
    fun getAll(): Flow<List<Acceso>>
}
