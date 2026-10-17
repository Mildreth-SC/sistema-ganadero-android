package com.ganadero.control.ui.produccion

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadero.control.data.Animal
import com.ganadero.control.data.Produccion
import com.ganadero.control.ui.components.BrownColor
import com.ganadero.control.ui.components.CampoForm
import com.ganadero.control.ui.components.ConfirmDialog
import com.ganadero.control.ui.components.DatoItem
import com.ganadero.control.ui.components.DatePickerField
import com.ganadero.control.ui.components.DialogBgColor
import com.ganadero.control.ui.components.Etiqueta
import com.ganadero.control.ui.components.PillBtn
import com.ganadero.control.ui.components.SelectorAnimal
import com.ganadero.control.ui.components.SuccessDialog
import com.ganadero.control.ui.oneDecimal
import com.ganadero.control.ui.todayStr
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

private val CardBg = Color(0xFFFDF7F3)

@Composable
fun ProduccionScreen(viewModel: ProduccionViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf<Produccion?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProduccionEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is ProduccionEvent.ShowSuccess ->
                    successMessage = event.message
            }
        }
    }

    ProduccionContent(
        state = uiState,
        onAdd = { showDialog = true },
        onSelect = { seleccionado = it },
        onDelete = viewModel::pedirEliminar
    )

    val animales = (uiState as? ProduccionUiState.Success)?.animales ?: emptyList()
    if (showDialog) {
        ProduccionDialog(
            animales = animales,
            onClose = { showDialog = false },
            onSave = {
                viewModel.addProduccion(it)
                showDialog = false
            }
        )
    }

    seleccionado?.let { p ->
        ProduccionDialog(
            produccion = p,
            animales = animales,
            onClose = { seleccionado = null },
            onSave = {
                viewModel.updateProduccion(it)
                seleccionado = null
            }
        )
    }

    (uiState as? ProduccionUiState.Success)?.porEliminar?.let { p ->
        ConfirmDialog(
            titulo = "Eliminar",
            mensaje = "¿Estás seguro de eliminar el registro de ${oneDecimal(p.litros)} L del ${p.fecha}?",
            onConfirm = viewModel::confirmarEliminar,
            onDismiss = viewModel::cancelarEliminar
        )
    }

    successMessage?.let { message ->
        SuccessDialog(mensaje = message, onDismiss = { successMessage = null })
    }
}

@Composable
private fun ProduccionContent(
    state: ProduccionUiState,
    onAdd: () -> Unit,
    onSelect: (Produccion) -> Unit,
    onDelete: (Produccion) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (state) {
            is ProduccionUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Orange)
            }
            is ProduccionUiState.Error -> {
                Text(state.message, Modifier.align(Alignment.Center), color = Danger)
            }
            is ProduccionUiState.Success -> {
                val registros = state.producciones
                val animales = state.animales
                val hoy = todayStr()
                val litrosHoy = registros.filter { it.fecha == hoy }.sumOf { it.litros }

                fun nombreAnimal(id: Int): String =
                    animales.firstOrNull { it.id == id }?.let { it.nombre.ifEmpty { it.codigo } } ?: "General"

                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    ProduccionHoyCard(litrosHoy)
                    Spacer(Modifier.height(16.dp))

                    if (registros.isEmpty()) {
                        Text(
                            "Sin registros de producción",
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(registros.sortedByDescending { it.fecha }, key = { it.id }) { p ->
                                ProduccionCard(
                                    p = p,
                                    animal = nombreAnimal(p.animalId),
                                    onClick = { onSelect(p) },
                                    onDelete = { onDelete(p) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            containerColor = Orange,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = Color.White)
        }
    }
}

// ---------------------------------------------------------------- TARJETAS

@Composable
private fun ProduccionHoyCard(litros: Double) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Producción de Hoy", fontSize = 12.sp, color = TextSecondary)
            Text(
                "${oneDecimal(litros)} L",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Orange
            )
        }
    }
}

@Composable
private fun ProduccionCard(
    p: Produccion,
    animal: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Orange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🥛", fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${oneDecimal(p.litros)} Litros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Animal: $animal", fontSize = 13.sp, color = TextSecondary)
                    Text("•", fontSize = 13.sp, color = TextSecondary)
                    Text("Turno: ${p.turno}", fontSize = 13.sp, color = TextSecondary)
                }
                Spacer(Modifier.height(2.dp))
                Text("Fecha: ${p.fecha}", fontSize = 12.sp, color = TextSecondary)
            }
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.2f))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = Orange,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// --------------------------------------------- DIÁLOGO

@Composable
private fun ProduccionDialog(
    produccion: Produccion? = null,
    animales: List<Animal>,
    onClose: () -> Unit,
    onSave: (Produccion) -> Unit
) {
    val esNuevo = produccion == null
    var editando by remember { mutableStateOf(esNuevo) }

    var litros by remember { mutableStateOf(produccion?.litros?.toString() ?: "") }
    var fecha by remember { mutableStateOf(produccion?.fecha ?: todayStr()) }
    var turno by remember { mutableStateOf(produccion?.turno ?: "Mañana") }
    var animalId by remember { mutableStateOf(produccion?.animalId ?: 0) }

    fun nombreAnimal(id: Int): String =
        animales.firstOrNull { it.id == id }?.let { it.nombre.ifEmpty { it.codigo } } ?: "General"

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DialogBgColor,
            modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = 620.dp)
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(
                    when {
                        esNuevo -> "Registrar Producción"
                        editando -> "Editar Producción"
                        else -> "Detalle de Producción"
                    },
                    fontSize = 17.sp,
                    color = BrownColor
                )
                Spacer(Modifier.height(10.dp))

                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (editando) {
                        CampoForm("Litros", litros, "Litros", KeyboardType.Decimal) { litros = it }
                        DatePickerField("Fecha", fecha, onFechaChange = { fecha = it })

                        Etiqueta("Turno")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PillBtn("Mañana", turno == "Mañana") { turno = "Mañana" }
                            PillBtn("Tarde", turno == "Tarde") { turno = "Tarde" }
                        }

                        Etiqueta("Animal")
                        SelectorAnimal(
                            seleccionado = nombreAnimal(animalId),
                            animales = animales,
                            onSelect = { animalId = it }
                        )
                    } else {
                        DatoItem("Litros", "${litros} L")
                        DatoItem("Fecha", fecha)
                        DatoItem("Turno", turno)
                        DatoItem("Animal", nombreAnimal(animalId))
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onClose) {
                        Text(
                            if (editando) "Cancelar" else "Cerrar",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (!editando) {
                                editando = true
                            } else {
                                val l = litros.toDoubleOrNull()
                                if (l != null) {
                                    onSave(
                                        produccion?.copy(
                                            animalId = animalId,
                                            fecha = fecha.trim(),
                                            turno = turno,
                                            litros = l
                                        ) ?: Produccion(
                                            animalId = animalId,
                                            fecha = fecha.trim(),
                                            turno = turno,
                                            litros = l
                                        )
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = CircleShape,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(if (editando) "Guardar" else "Editar", color = Color.White)
                    }
                }
            }
        }
    }
}
