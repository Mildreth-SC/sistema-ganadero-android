package com.ganadero.control.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganadero.control.data.Produccion
import com.ganadero.control.data.Repo
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

@Composable
fun ProduccionScreen() {
    var version by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    val registros = remember(version) { Repo.producciones.toList() }
    val hoy = todayStr()
    val litrosHoy = registros.filter { it.fecha == hoy }.sumOf { it.litros }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            StatCard("Producción de hoy", "${oneDecimal(litrosHoy)} L")
            Spacer(Modifier.height(12.dp))

            if (registros.isEmpty()) {
                Text("Sin registros de producción", color = TextSecondary, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(registros.sortedByDescending { it.fecha }, key = { it.id }) { p ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${oneDecimal(p.litros)} L", fontWeight = FontWeight.Bold, color = Orange, fontSize = 18.sp)
                                    Text("${Repo.animalName(p.animalId)} · ${p.turno}", fontSize = 13.sp, color = TextSecondary)
                                    Text(p.fecha, fontSize = 12.sp, color = TextSecondary)
                                }
                                IconButton(onClick = { Repo.deleteProduccion(p.id); version++ }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Danger)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = Orange,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = Color.White)
        }
    }

    if (showDialog) {
        ProduccionDialog(
            onClose = { showDialog = false },
            onSave = { Repo.addProduccion(it); version++; showDialog = false }
        )
    }
}

@Composable
private fun ProduccionDialog(onClose: () -> Unit, onSave: (Produccion) -> Unit) {
    var litros by remember { mutableStateOf("") }
    var turno by remember { mutableStateOf("Mañana") }
    var animalId by remember { mutableStateOf(0) }
    var fecha by remember { mutableStateOf(todayStr()) }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                val l = litros.toDoubleOrNull()
                if (l != null) onSave(Produccion(animalId = animalId, fecha = fecha, turno = turno, litros = l))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
        title = { Text("Registrar producción") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(litros, { litros = it }, label = { Text("Litros *") }, singleLine = true)
                OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true)

                Text("Turno", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegBtn("Mañana", turno == "Mañana") { turno = "Mañana" }
                    SegBtn("Tarde", turno == "Tarde") { turno = "Tarde" }
                }

                Text("Animal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegBtn("General", animalId == 0) { animalId = 0 }
                    Repo.animales.forEach { a ->
                        SegBtn(a.nombre.ifEmpty { a.codigo }, animalId == a.id) { animalId = a.id }
                    }
                }
            }
        }
    )
}
