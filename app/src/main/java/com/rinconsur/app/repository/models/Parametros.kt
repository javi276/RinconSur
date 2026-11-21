package com.rinconsur.app.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Parámetros del puesto de la persona encargada.
 * Es UNA sola fila (id = 1) que se reemplaza cada vez que se guarda.
 */
@Entity(
    tableName = "parametros",
    foreignKeys = [
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["accesoId"])]
)
data class Parametros(
    @PrimaryKey val id: Int = 1,
    val tienda: String,      // París, Lider, Sodimac, una zapatería, etc.
    val turno: String,       // Mañana / Tarde / Noche
    val horaInicio: String,  // HH:mm
    val horaFin: String,     // HH:mm
    val accesoId: Int        // FK -> accesos.id (acceso donde está la persona encargada)
)
