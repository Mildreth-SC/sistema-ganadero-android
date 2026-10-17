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
import androidx.compose.foundation.layout.width
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
import com.ganadero.control.data.Repo
import com.ganadero.control.data.Vacuna
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

@Composable
fun VacunasScreen() {
    var version by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    val vacunas = remember(version) { Repo.vacunas.toList() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            StatCard("Vacunas registradas", vacunas.size.toString())
            Spacer(Modifier.height(12.dp))

            if (vacunas.isEmpty()) {
                Text("Sin vacunas registradas", color = TextSecondary, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vacunas, key = { it.id }) { v ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💉", fontSize = 26.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(v.tipo, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${Repo.animalName(v.animalId)}", fontSize = 13.sp, color = TextSecondary)
                                    Text(
                                        "Aplicada: ${v.fechaAplicacion} · Próxima: ${v.proximaFecha.ifEmpty { "—" }}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(onClick = { Repo.deleteVacuna(v.id); version++ }) {
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
        VacunaDialog(
            onClose = { showDialog = false },
            onSave = { Repo.addVacuna(it); version++; showDialog = false }
        )
    }
}

@Composable
private fun VacunaDialog(onClose: () -> Unit, onSave: (Vacuna) -> Unit) {
    var tipo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(todayStr()) }
    var proxima by remember { mutableStateOf("") }
    var animalId by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                if (tipo.isNotBlank()) {
                    onSave(
                        Vacuna(
                            animalId = animalId,
                            tipo = tipo,
                            fechaAplicacion = fecha,
                            proximaFecha = proxima
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
        title = { Text("Registrar vacuna") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(tipo, { tipo = it }, label = { Text("Tipo de vacuna *") }, singleLine = true)
                OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha aplicación") }, singleLine = true)
                OutlinedTextField(proxima, { proxima = it }, label = { Text("Próxima dosis (opcional)") }, singleLine = true)

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
