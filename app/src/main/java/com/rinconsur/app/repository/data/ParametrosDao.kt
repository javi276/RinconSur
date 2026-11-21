package com.rinconsur.app.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rinconsur.app.repository.models.Parametros
import kotlinx.coroutines.flow.Flow

@Dao
interface ParametrosDao {
    // READ: null mientras la persona encargada aún no guarda sus parámetros
    @Query("SELECT * FROM parametros WHERE id = 1")
    fun get(): Flow<Parametros?>

    // CREATE / UPDATE: como el id siempre es 1, REPLACE actualiza la misma fila
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(parametros: Parametros)
}
