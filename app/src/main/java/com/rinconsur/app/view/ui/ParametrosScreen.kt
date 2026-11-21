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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.rinconsur.app.view.utils.AnimacionEntradaFormulario
import com.rinconsur.app.view.utils.CampoTexto
import com.rinconsur.app.view.utils.Selector
import com.rinconsur.app.view.utils.vibrar
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.ParametrosViewModel
import com.rinconsur.app.viewmodels.TURNOS
import kotlinx.coroutines.delay

/** Parámetros del puesto: se guardan en Room y se reutilizan en cada check-in. */
@Composable
fun ParametrosScreen(viewModel: ParametrosViewModel) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val accesos by viewModel.accesos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(form.guardado) {
        if (form.guardado) {
            vibrar(context)
            delay(2000)
            viewModel.limpiarGuardado()
        }
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
            Text("Configura tu puesto", style = MaterialTheme.typography.headlineMedium)
            Text("Estos datos se guardan en el teléfono y se usan en cada check-in.", style = MaterialTheme.typography.bodyMedium)

            AnimatedVisibility(visible = form.guardado) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Parámetros guardados correctamente", modifier = Modifier.padding(16.dp))
                }
            }

            CampoTexto(form.tienda, viewModel::onTienda, "Tienda", Icons.Default.Storefront, form.tiendaError, placeholder = "Ej: París, Lider, Sodimac")
            Selector("Turno", form.turno, TURNOS, viewModel::onTurno, form.turnoError, Icons.Default.Schedule)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(form.horaInicio, viewModel::onHoraInicio, "Inicio (HH:mm)", Icons.Default.AccessTime, form.inicioError, Modifier.weight(1f), placeholder = "08:00")
                CampoTexto(form.horaFin, viewModel::onHoraFin, "Fin (HH:mm)", Icons.Default.AccessTime, form.finError, Modifier.weight(1f), placeholder = "16:00")
            }
            Selector(
                etiqueta = "Mi acceso",
                seleccion = accesos.firstOrNull { it.id == form.accesoId }?.nombre ?: "",
                opciones = accesos.map { it.nombre },
                onSelect = { nombre -> accesos.firstOrNull { it.nombre == nombre }?.let { viewModel.onAcceso(it.id) } },
                error = form.accesoError,
                icono = Icons.Default.LocationOn
            )

            Button(
                onClick = { if (!viewModel.guardar()) vibrarError(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar parámetros") }
        }
    }
}
