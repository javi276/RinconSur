package com.rinconsur.app.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.app.repository.models.Registro
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    // READ (reactivo con Flow: la interfaz se actualiza sola al cambiar la tabla)
    @Query("SELECT * FROM registros ORDER BY id DESC")
    fun getAll(): Flow<List<Registro>>

    // READ de uno solo: para cargar el registro que se va a EDITAR
    @Query("SELECT * FROM registros WHERE id = :id")
    suspend fun getById(id: Int): Registro?

    // CREATE: check-in
    @Insert
    suspend fun insert(registro: Registro): Long

    // UPDATE: check-out y EDITAR un registro
    @Update
    suspend fun update(registro: Registro)

    // DELETE: desde el Historial
    @Delete
    suspend fun delete(registro: Registro)
}
