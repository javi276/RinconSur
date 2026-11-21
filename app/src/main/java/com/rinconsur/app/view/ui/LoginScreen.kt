package com.rinconsur.app.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.view.utils.CampoTexto
import com.rinconsur.app.view.utils.pulsoYRotacion
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit, onCrearCuenta: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.loggedUser) {
        if (state.loggedUser != null) onLoginSuccess()
    }

    // RECURSO NATIVO 2: el teléfono vibra si las credenciales son inválidas
    LaunchedEffect(state.error) {
        if (state.error != null) vibrarError(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = "Logo RinconSur",
            tint = MaterialTheme.colorScheme.primary,
            // ANIMACIÓN 2 (view/utils/Animations.kt): pulso + rotación del logo
            modifier = Modifier
                .size(96.dp)
                .pulsoYRotacion()
        )
        Spacer(Modifier.height(8.dp))
        Text("RinconSur", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Control de accesos de la tienda", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        CampoTexto(
            valor = state.email,
            onCambio = viewModel::onEmailChange,
            etiqueta = "Email",
            icono = Icons.Default.Email,
            error = state.emailError,
            teclado = KeyboardType.Email
        )
        Spacer(Modifier.height(8.dp))
        CampoTexto(
            valor = state.password,
            onCambio = viewModel::onPasswordChange,
            etiqueta = "Contraseña",
            icono = Icons.Default.Lock,
            error = state.passwordError,
            esClave = true,
            teclado = KeyboardType.Password
        )

        // El error general aparece con animación
        AnimatedVisibility(visible = state.error != null) {
            Text(
                state.error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::login,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCrearCuenta, modifier = Modifier.fillMaxWidth()) {
            Text("Crear cuenta de encargado/a")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Usuario de prueba: encargado@rinconsur.cl / rincon123",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
