package com.rinconsur.app.view.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * RECURSO NATIVO 2: vibración del teléfono.
 * - Vibración corta (150 ms) al guardar correctamente.
 * - Vibración larga (400 ms) cuando un formulario tiene errores o el login falla.
 * Usa el permiso VIBRATE declarado en el AndroidManifest (no requiere pedirlo en tiempo de ejecución).
 */
fun vibrar(context: Context, milisegundos: Long = 150) {
    val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 o superior: se obtiene desde VibratorManager
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrador == null || !vibrador.hasVibrator()) return
    vibrador.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE))
}

/** Vibración de error (más larga, se distingue de la de éxito). */
fun vibrarError(context: Context) = vibrar(context, 400)
