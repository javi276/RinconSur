package com.rinconsur.app.viewmodels

import android.util.Patterns

/**
 * Reglas de validación compartidas por los ViewModels (lógica separada de la vista).
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * La vista solo muestra el mensaje: nunca decide si un dato es correcto.
 */
object Validaciones {

    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    /**
     * Solo letras (incluye ñ y tildes) con UN espacio entre palabras.
     * Válidos: "Ana Pérez", "Rocío". Inválidos: " Ana", "Ana  Pérez", "Ana3".
     */
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")
    private val HORA = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private val TIENDA = Regex("^[A-Za-zÁÉÍÓÚáéíóúÜüÑñ0-9 .&'-]+$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    // ---------------- Cuenta y login ----------------
    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        !esSoloLetras(nombre) -> "Solo letras y un espacio entre palabras"
        else -> null
    }

    fun validarEmail(email: String): String? = when {
        email.isBlank() -> "El email es obligatorio"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Formato de email inválido"
        else -> null
    }

    fun validarPassword(pass: String): String? = when {
        pass.isBlank() -> "La contraseña es obligatoria"
        pass.length < 6 -> "Mínimo 6 caracteres"
        pass.contains(' ') -> "No debe contener espacios"
        else -> null
    }

    fun validarConfirmacion(pass: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        pass != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    // ---------------- Check-in / Check-out ----------------
    /** Para listas desplegables de texto (sexo, rango de edad, turno...). */
    fun validarSeleccion(valor: String, campo: String): String? =
        if (valor.isBlank()) "Selecciona $campo" else null

    /** Para el acceso: 0 significa "sin seleccionar". */
    fun validarAcceso(accesoId: Int, campo: String): String? =
        if (accesoId == 0) "Selecciona $campo" else null

    // ---------------- Parámetros ----------------
    fun validarTienda(tienda: String): String? = when {
        tienda.isBlank() -> "La tienda es obligatoria"
        tienda.trim().length < 2 -> "Mínimo 2 caracteres"
        !TIENDA.matches(tienda) -> "Solo letras, números y espacios"
        else -> null
    }

    fun validarHora(hora: String): String? = when {
        hora.isBlank() -> "La hora es obligatoria"
        !HORA.matches(hora) -> "Formato HH:mm (ej: 14:00)"
        else -> null
    }

    /** Un turno de noche puede cruzar la medianoche, por eso solo se exige que sean distintas. */
    fun validarHorario(inicio: String, fin: String): String? =
        if (inicio == fin) "La hora de término debe ser distinta" else null

    // ---------------- Comunicados (API Rest) ----------------
    fun validarTitulo(titulo: String): String? = when {
        titulo.isBlank() -> "El título es obligatorio"
        titulo.trim().length < 3 -> "Mínimo 3 caracteres"
        else -> null
    }

    fun validarCuerpo(cuerpo: String): String? = when {
        cuerpo.isBlank() -> "El mensaje es obligatorio"
        cuerpo.trim().length < 5 -> "Mínimo 5 caracteres"
        else -> null
    }
}
