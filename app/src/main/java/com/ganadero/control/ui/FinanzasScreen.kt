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
import com.ganadero.control.data.Finanza
import com.ganadero.control.data.Repo
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.Success
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

@Composable
fun FinanzasScreen() {
    var version by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    val movimientos = remember(version) { Repo.finanzas.toList() }
    val ingresos = movimientos.filter { it.tipo == "Ingreso" }.sumOf { it.monto }
    val gastos = movimientos.filter { it.tipo == "Gasto" }.sumOf { it.monto }
    val balance = ingresos - gastos

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Ingresos", money(ingresos), accent = Success, modifier = Modifier.weight(1f))
                StatCard("Gastos", money(gastos), accent = Danger, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            StatCard("Balance", money(balance), accent = if (balance >= 0) Success else Danger)
            Spacer(Modifier.height(12.dp))

            if (movimientos.isEmpty()) {
                Text("Sin movimientos registrados", color = TextSecondary, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(movimientos.sortedByDescending { it.fecha }, key = { it.id }) { f ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (f.tipo == "Ingreso") "📈" else "📉", fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(f.categoria.ifEmpty { f.tipo }, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    if (f.descripcion.isNotEmpty()) {
                                        Text(f.descripcion, fontSize = 13.sp, color = TextSecondary)
                                    }
                                    Text(f.fecha, fontSize = 12.sp, color = TextSecondary)
                                }
                                Text(
                                    (if (f.tipo == "Ingreso") "+" else "-") + money(f.monto),
                                    fontWeight = FontWeight.Bold,
                                    color = if (f.tipo == "Ingreso") Success else Danger
                                )
                                IconButton(onClick = { Repo.deleteFinanza(f.id); version++ }) {
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
        FinanzaDialog(
            onClose = { showDialog = false },
            onSave = { Repo.addFinanza(it); version++; showDialog = false }
        )
    }
}

@Composable
private fun FinanzaDialog(onClose: () -> Unit, onSave: (Finanza) -> Unit) {
    var tipo by remember { mutableStateOf("Ingreso") }
    var categoria by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(todayStr()) }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                val m = monto.toDoubleOrNull()
                if (m != null) {
                    onSave(
                        Finanza(
                            fecha = fecha,
                            tipo = tipo,
                            categoria = categoria,
                            monto = m,
                            descripcion = descripcion
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
        title = { Text("Nuevo movimiento") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tipo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegBtn("Ingreso", tipo == "Ingreso") { tipo = "Ingreso" }
                    SegBtn("Gasto", tipo == "Gasto") { tipo = "Gasto" }
                }
                OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoría") }, singleLine = true)
                OutlinedTextField(monto, { monto = it }, label = { Text("Monto *") }, singleLine = true)
                OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true)
                OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, singleLine = true)
            }
        }
    )
}
