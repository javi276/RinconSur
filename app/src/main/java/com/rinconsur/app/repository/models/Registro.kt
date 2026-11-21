package com.rinconsur.app.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una fila = una persona que entra a la tienda (check-in) y luego sale (check-out).
 * Mientras la persona sigue dentro, los campos de salida quedan en null.
 */
@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoEntradaId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoSalidaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["accesoEntradaId"]), Index(value = ["accesoSalidaId"])]
)
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // ---- Check-in ----
    val accesoEntradaId: Int,              // FK -> accesos.id
    val sexo: String,                      // Femenino / Masculino
    val acompanado: Boolean,               // true = entra acompañada
    val tipoAcompanante: String? = null,   // Adulto / Menor (solo si va acompañada)
    val sexoAcompanante: String? = null,   // Femenino / Masculino (solo si va acompañada)
    val rangoEtario: String,               // 18-25, 26-35, 36-50, 51+
    val checkIn: String,                   // fecha y hora de entrada
    val encargado: String,                 // quién hizo el registro
    val tienda: String,                    // copiado desde Parámetros
    val turno: String,                     // copiado desde Parámetros
    val fotoUri: String? = null,           // foto del acceso tomada con CameraX (opcional)
    // ---- Check-out (null mientras la persona sigue dentro) ----
    val accesoSalidaId: Int? = null,       // FK -> accesos.id
    val checkOut: String? = null,
    val compro: Boolean? = null            // true = compró, false = solo transitó
)
