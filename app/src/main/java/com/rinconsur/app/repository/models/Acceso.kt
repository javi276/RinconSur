package com.rinconsur.app.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Acceso (puerta) de la tienda. La tienda tiene 4 accesos fijos que se cargan
 * al abrir la base de datos (ver AppDatabase.seedCallback).
 * Los registros y los parámetros apuntan a esta tabla con una llave foránea (FK).
 */
@Entity(tableName = "accesos")
data class Acceso(
    @PrimaryKey val id: Int,   // 1 a 4
    val nombre: String         // Ej: "Acceso 1 - Principal"
)
