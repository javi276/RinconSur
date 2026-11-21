package com.rinconsur.app.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

/**
 * EDITAR REGISTRO: el mismo formulario del check-in, pero ya lleno con los datos guardados.
 * Sirve para corregir un error después de registrar a una persona.
 */
@Composable
fun EditarRegistroScreen(
    viewModel: RegistroViewModel,
    registroId: Int,      // llega desde la ruta "editar_registro/{id}"
    onDone: () -> Unit    // vuelve al Historial
) {
    val form by viewModel.editar.collectAsStateWithLifecycle()
    val accesos by viewModel.accesos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    // Al abrir la pantalla: se busca el registro en Room y se precarga el formulario
    LaunchedEffect(registroId) {
        viewModel.cargarParaEditar(registroId)
        visible = true
    }
    // Al salir de la pantalla: se limpia el formulario
    DisposableEffect(Unit) { onDispose { viewModel.limpiarEdicion() } }

    // Al guardar: vibración corta (RECURSO NATIVO 2) y vuelve al Historial
    LaunchedEffect(form.guardado) {
        if (form.guardado) {
            vibrar(context)
            onDone()
        }
    }

    // RECURSO NATIVO 1: cámara, por si quiere cambiar la foto
    if (showCamera) {
        CameraCapture(
            carpeta = "registros",
            onImageCaptured = { uri ->
                viewModel.onEditFoto(uri.toString())
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    val original = form.original
    // Mientras Room entrega el registro, se muestra un indicador de carga
    if (original == null || original.id != registroId) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
            Text("Editar registro #${original.id}", style = MaterialTheme.typography.headlineMedium)
            // Datos que NO se editan (los puso la app sola)
            Text("Entrada: ${original.checkIn} · Registró: ${original.encargado}", style = MaterialTheme.typography.bodyMedium)

            PhotoBox(imageUri = form.fotoUri, circle = false, onTakePhoto = { showCamera = true })

            // ----- Datos de la ENTRADA (los mismos campos del check-in) -----
            Selector(
                etiqueta = "Acceso de entrada",
                seleccion = accesos.firstOrNull { it.id == form.accesoId }?.nombre ?: "",
                opciones = accesos.map { it.nombre },
                onSelect = { nombre -> accesos.firstOrNull { it.nombre == nombre }?.let { viewModel.onEditAcceso(it.id) } },
                error = form.accesoError,
                icono = Icons.AutoMirrored.Filled.Login
            )
            Selector("Sexo", form.sexo, SEXOS, viewModel::onEditSexo, form.sexoError, Icons.Default.Face)
            Selector("Rango de edad", form.rango, RANGOS, viewModel::onEditRango, form.rangoError, Icons.Default.Cake)

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("¿Entra acompañado/a?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(checked = form.acompanado, onCheckedChange = viewModel::onEditAcompanado)
            }
            AnimatedVisibility(visible = form.acompanado) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Selector("Tipo de acompañante", form.tipoAcomp, TIPOS_ACOMPANANTE, viewModel::onEditTipoAcomp, form.tipoAcompError, Icons.Default.People)
                    Selector("Sexo del acompañante", form.sexoAcomp, SEXOS, viewModel::onEditSexoAcomp, form.sexoAcompError, Icons.Default.Face)
                }
            }

            // ----- Datos de la SALIDA: solo aparecen si la persona ya hizo check-out -----
            if (original.checkOut != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Salida: ${original.checkOut}", style = MaterialTheme.typography.titleSmall)
                        Selector(
                            etiqueta = "Acceso de salida",
                            seleccion = accesos.firstOrNull { it.id == form.accesoSalidaId }?.nombre ?: "",
                            opciones = accesos.map { it.nombre },
                            onSelect = { nombre -> accesos.firstOrNull { it.nombre == nombre }?.let { viewModel.onEditAccesoSalida(it.id) } },
                            error = form.accesoSalidaError,
                            icono = Icons.AutoMirrored.Filled.ExitToApp
                        )
                        Text("¿Realizó una compra?", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = form.compro, onClick = { viewModel.onEditCompro(true) }, label = { Text("Sí, compró") })
                            FilterChip(selected = !form.compro, onClick = { viewModel.onEditCompro(false) }, label = { Text("No compró") })
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val ok = viewModel.guardarEdicion()
                    if (!ok) vibrarError(context) // si hay errores, vibración larga como aviso
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cambios") }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
