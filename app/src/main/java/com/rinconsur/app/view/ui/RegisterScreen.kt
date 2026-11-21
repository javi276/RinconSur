package com.rinconsur.app.view.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.view.utils.AnimacionEntradaFormulario
import com.rinconsur.app.view.utils.CampoTexto
import com.rinconsur.app.view.utils.vibrar
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.UserViewModel

/** Formulario "Crear cuenta" de la persona encargada. */
@Composable
fun RegisterScreen(viewModel: UserViewModel, onDone: () -> Unit) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    // Al salir de la pantalla se limpia el formulario
    DisposableEffect(Unit) { onDispose { viewModel.clearForm() } }
    // Error de la base de datos (email repetido): vibración de error
    LaunchedEffect(form.message) { if (form.message != null) vibrarError(context) }

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
            Text("Nueva cuenta de encargado/a", style = MaterialTheme.typography.headlineSmall)

            CampoTexto(form.name, viewModel::onNameChange, "Nombre completo", Icons.Default.Person, form.nameError, placeholder = "Ej: Ana Pérez")
            CampoTexto(form.email, viewModel::onEmailChange, "Email", Icons.Default.Email, form.emailError, teclado = KeyboardType.Email)
            CampoTexto(form.password, viewModel::onPasswordChange, "Contraseña", Icons.Default.Lock, form.passwordError, esClave = true, teclado = KeyboardType.Password)
            CampoTexto(form.confirm, viewModel::onConfirmChange, "Confirmar contraseña", Icons.Default.Lock, form.confirmError, esClave = true, teclado = KeyboardType.Password)

            AnimatedVisibility(visible = form.message != null) {
                Text(form.message.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val ok = viewModel.saveUser(onSuccess = {
                        vibrar(context)
                        Toast.makeText(context, "Cuenta creada. Ahora inicia sesión.", Toast.LENGTH_SHORT).show()
                        onDone()
                    })
                    if (!ok) vibrarError(context)
                },
                enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (form.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Crear cuenta")
            }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
