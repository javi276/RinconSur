package com.rinconsur.app.view.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/*
 * Campos de formulario reutilizables con retroalimentación visual por campo:
 *   - con error: borde rojo + ícono de error + mensaje debajo
 *   - válido y con datos: ícono verde de "correcto"
 * El mensaje de error NO se calcula aquí: llega desde el ViewModel (Validaciones.kt).
 */

/** Mensaje de error con ícono (se usa como supportingText de los campos). */
@Composable
private fun MensajeError(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Error, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(4.dp))
        Text(texto)
    }
}

@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    esClave: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    placeholder: String? = null
) {
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val valido = error == null && valor.isNotBlank()

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            when {
                esClave -> IconButton(onClick = { mostrarClave = !mostrarClave }) {
                    Icon(
                        if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
                error != null -> Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
                valido -> Icon(Icons.Filled.CheckCircle, contentDescription = "Campo correcto", tint = MaterialTheme.colorScheme.tertiary)
            }
        },
        isError = error != null,
        supportingText = error?.let { { MensajeError(it) } },
        visualTransformation = if (esClave && !mostrarClave) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

/** Lista desplegable (igual al selector de tipo de usuario del proyecto base), con error por campo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Selector(
    etiqueta: String,
    seleccion: String,
    opciones: List<String>,
    onSelect: (String) -> Unit,
    error: String?,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
                else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            isError = error != null,
            supportingText = error?.let { { MensajeError(it) } },
            singleLine = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSelect(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
