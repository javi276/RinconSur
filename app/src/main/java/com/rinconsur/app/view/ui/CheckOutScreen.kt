package com.rinconsur.app.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.repository.models.Registro
import com.rinconsur.app.view.utils.Selector
import com.rinconsur.app.view.utils.vibrar
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.RegistroViewModel
import kotlinx.coroutines.delay

/** CHECK-OUT: lista de personas dentro; se toca una y se registra su salida. */
@Composable
fun CheckOutScreen(viewModel: RegistroViewModel) {
    val dentro by viewModel.dentro.collectAsStateWithLifecycle()
    val estado by viewModel.checkOut.collectAsStateWithLifecycle()
    val accesos by viewModel.accesos.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            vibrar(context)
            delay(2000)
            viewModel.limpiarCheckOutGuardado()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = estado.guardado) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Salida registrada correctamente", modifier = Modifier.padding(16.dp))
            }
        }
        Text(
            "${dentro.size} persona(s) dentro ahora",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (dentro.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay personas dentro. Registra una entrada primero.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dentro, key = { it.id }) { registro ->
                    PersonaCard(
                        registro = registro,
                        nombreAcceso = viewModel.nombreAcceso(registro.accesoEntradaId),
                        seleccionado = estado.seleccionado?.id == registro.id,
                        onClick = { viewModel.seleccionarParaSalida(registro) }
                    )
                }
            }
        }
    }

    // Diálogo para completar la salida de la persona elegida
    estado.seleccionado?.let { elegido ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarSalida,
            title = { Text("Registrar salida #${elegido.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Entró: ${elegido.checkIn}")
                    Selector(
                        etiqueta = "Acceso de salida",
                        seleccion = accesos.firstOrNull { it.id == estado.accesoSalidaId }?.nombre ?: "",
                        opciones = accesos.map { it.nombre },
                        onSelect = { nombre -> accesos.firstOrNull { it.nombre == nombre }?.let { viewModel.onAccesoSalida(it.id) } },
                        error = estado.accesoError,
                        icono = Icons.AutoMirrored.Filled.ExitToApp
                    )
                    Text("¿Realizó una compra?", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = estado.compro, onClick = { viewModel.onCompro(true) }, label = { Text("Sí, compró") })
                        FilterChip(selected = !estado.compro, onClick = { viewModel.onCompro(false) }, label = { Text("No compró") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (!viewModel.confirmarCheckOut()) vibrarError(context) }) { Text("Registrar salida") }
            },
            dismissButton = { TextButton(onClick = viewModel::cancelarSalida) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PersonaCard(registro: Registro, nombreAcceso: String, seleccionado: Boolean, onClick: () -> Unit) {
    // ANIMACIÓN: la tarjeta tocada cambia de color suavemente
    val color by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "colorPersona"
    )
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Entrada #${registro.id} · ${registro.checkIn}", style = MaterialTheme.typography.titleSmall)
                Text("${registro.sexo} · ${registro.rangoEtario} años · ${if (registro.acompanado) "Acompañado/a" else "Solo/a"}")
                Text(nombreAcceso, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
