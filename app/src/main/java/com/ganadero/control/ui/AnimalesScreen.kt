package com.ganadero.control.ui

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
import com.ganadero.control.data.Animal
import com.ganadero.control.data.Repo
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

@Composable
fun AnimalesScreen() {
    var version by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    val animales = remember(version) { Repo.animales.toList() }

    Box(Modifier.fillMaxSize()) {
        if (animales.isEmpty()) {
            Text(
                "No hay animales registrados",
                Modifier.align(Alignment.Center),
                color = TextSecondary
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(animales, key = { it.id }) { a ->
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (a.sexo == "Macho") "🐂" else "🐄", fontSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${a.codigo}${if (a.nombre.isNotEmpty()) " · ${a.nombre}" else ""}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "${a.raza.ifEmpty { "Sin raza" }} · ${a.tipo} · ${a.pesoActual} kg",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                if (a.madreNombre.isNotEmpty() || a.padreNombre.isNotEmpty()) {
                                    Text(
                                        "Madre: ${a.madreNombre.ifEmpty { "—" }} · Padre: ${a.padreNombre.ifEmpty { "—" }}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = {
                                Repo.deleteAnimal(a.id)
                                version++
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Danger)
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
        AnimalDialog(
            onClose = { showDialog = false },
            onSave = {
                Repo.addAnimal(it)
                version++
                showDialog = false
            }
        )
    }
}

@Composable
private fun AnimalDialog(onClose: () -> Unit, onSave: (Animal) -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Leche") }
    var sexo by remember { mutableStateOf("Hembra") }
    var madre by remember { mutableStateOf("") }
    var padre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                if (codigo.isNotBlank()) {
                    onSave(
                        Animal(
                            codigo = codigo,
                            nombre = nombre,
                            raza = raza,
                            pesoActual = peso.toDoubleOrNull() ?: 0.0,
                            tipo = tipo,
                            sexo = sexo,
                            madreNombre = madre,
                            padreNombre = padre
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
        title = { Text("Nuevo animal") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(codigo, { codigo = it }, label = { Text("Código / arete *") }, singleLine = true)
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, singleLine = true)
                OutlinedTextField(peso, { peso = it }, label = { Text("Peso (kg)") }, singleLine = true)

                Text("Sexo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegBtn("Hembra", sexo == "Hembra") { sexo = "Hembra" }
                    SegBtn("Macho", sexo == "Macho") { sexo = "Macho" }
                }

                Text("Tipo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegBtn("Leche", tipo == "Leche") { tipo = "Leche" }
                    SegBtn("Carne", tipo == "Carne") { tipo = "Carne" }
                    SegBtn("Mixto", tipo == "Mixto") { tipo = "Mixto" }
                }

                Text("Genealogía", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                OutlinedTextField(madre, { madre = it }, label = { Text("Nombre de la madre") }, singleLine = true)
                OutlinedTextField(padre, { padre = it }, label = { Text("Nombre del padre") }, singleLine = true)
            }
        }
    )
}
