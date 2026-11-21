package com.rinconsur.app.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Persona encargada de registrar entradas y salidas (quien inicia sesión). */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // no se repiten correos
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String // Solo para fines académicos (sin hash)
)
