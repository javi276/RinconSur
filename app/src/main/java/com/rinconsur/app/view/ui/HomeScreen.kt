package com.rinconsur.app.view.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.app.navegations.Routes
import com.rinconsur.app.navegations.menuItems
import com.rinconsur.app.repository.models.Parametros
import com.rinconsur.app.view.utils.ContadorAnimado
import com.rinconsur.app.viewmodels.RegistroViewModel

/** Tablero de la persona encargada: métricas, accesos rápidos, resumen por acceso y perfil. */
@Composable
fun HomeScreen(
    userName: String,
    viewModel: RegistroViewModel,
    parametros: Parametros?,
    onNavigate: (String) -> Unit
) {
    val dentro by viewModel.dentro.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()
    val entradas = resumen.sumOf { it.entradas }
    val salidas = resumen.sumOf { it.salidas }
    val compras = resumen.sumOf { it.compran }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (parametros == null) "Antes de registrar, configura los parámetros de tu puesto."
                else "${parametros.tienda} · Turno ${parametros.turno} (${parametros.horaInicio} a ${parametros.horaFin})",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Aviso si faltan los parámetros
        if (parametros == null) {
            item {
                Card(
                    onClick = { onNavigate(Routes.PARAMETROS) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(12.dp))
                        Text("Toca aquí para configurar tienda, turno y acceso.", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // Métricas (ANIMACIÓN 4: los números cambian con animación)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metrica("Dentro", dentro.size, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                Metrica("Entradas", entradas, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metrica("Salidas", salidas, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                Metrica("Compras", compras, Color(0xFFE0A12B), Modifier.weight(1f))
            }
        }

        // Accesos rápidos (mismos destinos que el menú lateral, excepto Inicio)
        item { Text("¿Qué deseas hacer?", style = MaterialTheme.typography.titleMedium) }
        items(menuItems.filter { it.route != Routes.HOME }) { item ->
            Card(onClick = { onNavigate(item.route) }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(item.label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Resumen por acceso
        item { Text("Resumen por acceso", style = MaterialTheme.typography.titleMedium) }
        items(resumen) { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(r.acceso.nombre, style = MaterialTheme.typography.titleSmall)
                    Text("${r.entradas} entradas · ${r.salidas} salidas")
                    Text("${r.compran} compran · ${r.transitan} solo transitan", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Perfil de quienes solo transitan (el objetivo del caso: saber quién entra y no compra)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Perfil de quienes solo transitan", style = MaterialTheme.typography.titleMedium)
                    Text("Total: ${perfil.total}")
                    Text("Femenino: ${perfil.femenino} · Masculino: ${perfil.masculino}")
                    Text("Solos: ${perfil.solos} · Acompañados: ${perfil.acompanados}")
                    Text("Acompañantes: ${perfil.acompAdultos} adultos · ${perfil.acompMenores} menores")
                    perfil.porRango.forEach { (rango, cantidad) -> Text("$rango años: $cantidad") }
                }
            }
        }
    }
}

@Composable
private fun Metrica(titulo: String, valor: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContadorAnimado(valor = valor, color = color)
            Text(titulo, style = MaterialTheme.typography.labelLarge)
        }
    }
}
