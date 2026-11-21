package com.rinconsur.app.view.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rinconsur.app.repository.models.Registro
import com.rinconsur.app.viewmodels.RegistroViewModel

/** Historial de todos los registros, con búsqueda, filtro, edición y eliminación. */
@Composable
fun HistorialScreen(
    viewModel: RegistroViewModel,
    onEditar: (Int) -> Unit // abre la pantalla Editar con el id del registro
) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    var consulta by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var toDelete by remember { mutableStateOf<Registro?>(null) }

    val filtrados = registros.filter { r ->
        val texto = "${r.id} ${r.sexo} ${r.rangoEtario} ${r.tienda} ${r.turno} ${r.encargado}".lowercase()
        val coincideTexto = consulta.isBlank() || texto.contains(consulta.trim().lowercase())
        val coincideFiltro = when (filtro) {
            "Dentro" -> r.checkOut == null
            "Finalizados" -> r.checkOut != null
            else -> true
        }
        coincideTexto && coincideFiltro
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = consulta,
            onValueChange = { consulta = it },
            label = { Text("Buscar (tienda, sexo, edad, encargado)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Dentro", "Finalizados").forEach { opcion ->
                FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion) })
            }
        }

        if (filtrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (registros.isEmpty()) "Aún no hay registros." else "No hay coincidencias.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados, key = { it.id }) { r ->
                    // ANIMACIÓN: las tarjetas se reacomodan suavemente al buscar o filtrar
                    Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Foto tomada con la cámara (recortada y con bordes redondeados)
                            if (r.fotoUri != null) {
                                AsyncImage(
                                    model = r.fotoUri,
                                    contentDescription = "Foto del registro ${r.id}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(64.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Registro #${r.id} · ${if (r.checkOut == null) "Dentro" else "Finalizado"}", style = MaterialTheme.typography.titleSmall)
                                Text("${r.sexo} · ${r.rangoEtario} años · ${if (r.acompanado) "Acompañado/a" else "Solo/a"}")
                                if (r.acompanado) Text("Acompañante: ${r.tipoAcompanante} · ${r.sexoAcompanante}", style = MaterialTheme.typography.bodySmall)
                                Text("Entrada: ${viewModel.nombreAcceso(r.accesoEntradaId)} · ${r.checkIn}", style = MaterialTheme.typography.bodySmall)
                                if (r.checkOut != null) {
                                    Text("Salida: ${viewModel.nombreAcceso(r.accesoSalidaId)} · ${r.checkOut}", style = MaterialTheme.typography.bodySmall)
                                    Text(if (r.compro == true) "Realizó una compra" else "Solo transitó", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${r.tienda} · Turno ${r.turno} · Registró: ${r.encargado}", style = MaterialTheme.typography.labelSmall)
                            }
                            Column {
                                // UPDATE: lápiz para corregir el registro
                                IconButton(onClick = { onEditar(r.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { toDelete = r }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DELETE con confirmación
    toDelete?.let { registro ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar registro") },
            text = { Text("¿Eliminar el registro #${registro.id}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarRegistro(registro)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
