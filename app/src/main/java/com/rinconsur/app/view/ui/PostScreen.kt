package com.rinconsur.app.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.repository.models.Post
import com.rinconsur.app.view.utils.CampoTexto
import com.rinconsur.app.view.utils.vibrar
import com.rinconsur.app.view.utils.vibrarError
import com.rinconsur.app.viewmodels.PostViewModel

/**
 * PANTALLA DE LA API REST (Guía 14).
 * Muestra los "Comunicados" que llegan desde https://jsonplaceholder.typicode.com/posts
 *   GET    -> al abrir la pantalla y con el botón Recargar
 *   POST   -> botón "Nuevo"
 *   PUT    -> lápiz de cada tarjeta
 *   DELETE -> basurero de cada tarjeta
 * La barra superior (TopAppBar) ya la pone AppNavigation, igual que en las otras pantallas.
 */
@Composable
fun PostScreen(viewModel: PostViewModel) {
    // Observamos el flujo de datos del ViewModel
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Post?>(null) }

    // Muestra un aviso abajo cuando el ViewModel deja un mensaje (creado, editado, eliminado)
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            vibrar(context) // RECURSO NATIVO: vibración corta como confirmación
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0), // los márgenes del sistema ya los aplica AppNavigation
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.abrirNuevo() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                // 1) Cargando: rueda de progreso
                state.cargando -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Cargando comunicados...")
                }

                // 2) Error: mensaje + botón Reintentar
                state.error != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(state.error ?: "", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.fetchPosts() }) { Text("Reintentar") }
                }

                // 3) Datos: lista de publicaciones
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Encabezado(total = state.posts.size, onRecargar = { viewModel.fetchPosts() }) }
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onEditar = { viewModel.abrirEditar(post) },
                            onEliminar = { porEliminar = post },
                            modifier = Modifier.animateItem() // ANIMACIÓN: la tarjeta entra/sale suave
                        )
                    }
                }
            }
        }
    }

    // Formulario (POST / PUT) en un diálogo
    if (form.visible) {
        AlertDialog(
            onDismissRequest = { if (!form.enviando) viewModel.cerrarFormulario() },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
            title = { Text(if (form.id == 0) "Nuevo comunicado" else "Editar comunicado #${form.id}") },
            text = {
                Column {
                    CampoTexto(
                        valor = form.titulo,
                        onCambio = viewModel::onTitulo,
                        etiqueta = "Título",
                        icono = Icons.Default.Title,
                        error = form.tituloError
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = form.cuerpo,
                        onCambio = viewModel::onCuerpo,
                        etiqueta = "Mensaje",
                        icono = Icons.AutoMirrored.Filled.Notes,
                        error = form.cuerpoError
                    )
                    AnimatedVisibility(visible = form.enviando) {
                        Text("Enviando a la API...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !form.enviando,
                    onClick = { if (!viewModel.guardar()) vibrarError(context) } // si hay errores, vibración larga
                ) { Text(if (form.id == 0) "Publicar (POST)" else "Guardar (PUT)") }
            },
            dismissButton = {
                TextButton(enabled = !form.enviando, onClick = { viewModel.cerrarFormulario() }) { Text("Cancelar") }
            }
        )
    }

    // Confirmación antes de eliminar (DELETE)
    porEliminar?.let { post ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar comunicado #${post.id}?") },
            text = { Text(post.title) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(post)
                    porEliminar = null
                }) { Text("Eliminar (DELETE)") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Encabezado(total: Int, onRecargar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Comunicados desde la API Rest", style = MaterialTheme.typography.titleMedium)
                Text("jsonplaceholder.typicode.com/posts · $total publicaciones", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRecargar) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar (GET)")
            }
        }
    }
}

@Composable
private fun PostCard(post: Post, onEditar: () -> Unit, onEliminar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("#${post.id} · ${post.title}", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(post.body, style = MaterialTheme.typography.bodyMedium)
            }
            Column {
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
