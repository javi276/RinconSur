package com.rinconsur.app.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.repository.models.Parametros
import com.rinconsur.app.view.utils.AnimacionEntradaFormulario
import com.rinconsur.app.view.utils.CameraCapture
import com.rinconsur.app.view.utils.PhotoBox
import com.rinconsur.app.view.utils.Selector
import com.rinconsur.app.view.utils.vibrar
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.RANGOS
import com.rinconsur.app.viewmodels.RegistroViewModel
import com.rinconsur.app.viewmodels.SEXOS
import com.rinconsur.app.viewmodels.TIPOS_ACOMPANANTE
import kotlinx.coroutines.delay

/** Formulario de CHECK-IN: registra a una persona que entra a la tienda. */
@Composable
fun CheckInScreen(
    viewModel: RegistroViewModel,
    encargado: String,
    parametros: Parametros?,
    onIrParametros: () -> Unit
) {
    val form by viewModel.checkIn.collectAsStateWithLifecycle()
    val accesos by viewModel.accesos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // El acceso de entrada parte con el acceso de la persona encargada (desde Parámetros)
    LaunchedEffect(parametros, form.accesoId) {
        if (form.accesoId == 0 && parametros != null) viewModel.onAcceso(parametros.accesoId)
    }

    // Al guardar: vibración corta (RECURSO NATIVO 2) y el aviso se oculta a los 2 segundos
    LaunchedEffect(form.guardado) {
        if (form.guardado) {
            vibrar(context)
            delay(2000)
            viewModel.limpiarCheckInGuardado()
        }
    }

    // RECURSO NATIVO 1: cámara (CameraX). La foto se guarda en files/fotos/registros/
    if (showCamera) {
        CameraCapture(
            carpeta = "registros",
            onImageCaptured = { uri ->
                viewModel.onFoto(uri.toString())
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

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
            Text("Registrar entrada", style = MaterialTheme.typography.headlineMedium)

            // Aviso de guardado (aparece y desaparece con animación)
            AnimatedVisibility(visible = form.guardado) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrada registrada correctamente", modifier = Modifier.padding(16.dp))
                }
            }

            if (parametros == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Antes de registrar, configura los parámetros del puesto.", color = MaterialTheme.colorScheme.onErrorContainer)
                        OutlinedButton(onClick = onIrParametros, modifier = Modifier.fillMaxWidth()) { Text("Ir a Parámetros") }
                    }
                }
            } else {
                Text("${parametros.tienda} · Turno ${parametros.turno}", style = MaterialTheme.typography.bodyMedium)
            }

            // Foto opcional del acceso (toca el recuadro para abrir la cámara)
            PhotoBox(imageUri = form.fotoUri, circle = false, onTakePhoto = { showCamera = true })

            Selector(
                etiqueta = "Acceso de entrada",
                seleccion = accesos.firstOrNull { it.id == form.accesoId }?.nombre ?: "",
                opciones = accesos.map { it.nombre },
                onSelect = { nombre -> accesos.firstOrNull { it.nombre == nombre }?.let { viewModel.onAcceso(it.id) } },
                error = form.accesoError,
                icono = Icons.AutoMirrored.Filled.Login
            )
            Selector("Sexo", form.sexo, SEXOS, viewModel::onSexo, form.sexoError, Icons.Default.Face)
            Selector("Rango de edad", form.rango, RANGOS, viewModel::onRango, form.rangoError, Icons.Default.Cake)

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("¿Entra acompañado/a?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(checked = form.acompanado, onCheckedChange = viewModel::onAcompanado)
            }
            // Los datos del acompañante solo aparecen (con animación) si va acompañado/a
            AnimatedVisibility(visible = form.acompanado) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Selector("Tipo de acompañante", form.tipoAcomp, TIPOS_ACOMPANANTE, viewModel::onTipoAcomp, form.tipoAcompError, Icons.Default.People)
                    Selector("Sexo del acompañante", form.sexoAcomp, SEXOS, viewModel::onSexoAcomp, form.sexoAcompError, Icons.Default.Face)
                }
            }

            Button(
                onClick = {
                    val ok = viewModel.guardarCheckIn(encargado, parametros)
                    if (!ok) vibrarError(context) // si hay errores, vibración larga como aviso
                },
                enabled = parametros != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Registrar entrada") }
        }
    }
}
