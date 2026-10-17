package com.ganadero.control.ui.vacunas

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadero.control.data.Animal
import com.ganadero.control.data.Vacuna
import com.ganadero.control.ui.components.BrownColor
import com.ganadero.control.ui.components.CampoForm
import com.ganadero.control.ui.components.ConfirmDialog
import com.ganadero.control.ui.components.DatePickerField
import com.ganadero.control.ui.components.DatoItem
import com.ganadero.control.ui.components.DialogBgColor
import com.ganadero.control.ui.components.Etiqueta
import com.ganadero.control.ui.components.SelectorAnimal
import com.ganadero.control.ui.components.SuccessDialog
import com.ganadero.control.ui.todayStr
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

private val CardBg = Color(0xFFFDF7F3)

@Composable
fun VacunasScreen(viewModel: VacunasViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var seleccionada by remember { mutableStateOf<Vacuna?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is VacunasEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is VacunasEvent.ShowSuccess ->
                    successMessage = event.message
            }
        }
    }

    VacunasContent(
        state = uiState,
        onAdd = { showDialog = true },
        onSelect = { seleccionada = it },
        onDelete = viewModel::pedirEliminar
    )

    val animales = (uiState as? VacunasUiState.Success)?.animales ?: emptyList()
    if (showDialog) {
        VacunaDialog(
            animales = animales,
            onClose = { showDialog = false },
            onSave = {
                viewModel.addVacuna(it)
                showDialog = false
            }
        )
    }

    seleccionada?.let { v ->
        VacunaDialog(
            vacuna = v,
            animales = animales,
            onClose = { seleccionada = null },
            onSave = {
                viewModel.updateVacuna(it)
                seleccionada = null
            }
        )
    }

    (uiState as? VacunasUiState.Success)?.porEliminar?.let { v ->
        ConfirmDialog(
            titulo = "Eliminar",
            mensaje = "¿Estás seguro de eliminar la vacuna \"${v.tipo}\"?",
            onConfirm = viewModel::confirmarEliminar,
            onDismiss = viewModel::cancelarEliminar
        )
    }

    successMessage?.let { message ->
        SuccessDialog(mensaje = message, onDismiss = { successMessage = null })
    }
}

@Composable
private fun VacunasContent(
    state: VacunasUiState,
    onAdd: () -> Unit,
    onSelect: (Vacuna) -> Unit,
    onDelete: (Vacuna) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (state) {
            is VacunasUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Orange)
            }
            is VacunasUiState.Error -> {
                Text(state.message, Modifier.align(Alignment.Center), color = Danger)
            }
            is VacunasUiState.Success -> {
                val vacunas = state.vacunas
                val animales = state.animales

                fun nombreAnimal(id: Int): String =
                    animales.firstOrNull { it.id == id }?.let { it.nombre.ifEmpty { it.codigo } } ?: "General"

                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    VacunasCountCard(vacunas.size)
                    Spacer(Modifier.height(16.dp))

                    if (vacunas.isEmpty()) {
                        Text(
                            "Sin vacunas registradas",
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(vacunas, key = { it.id }) { v ->
                                VacunaCard(
                                    v = v,
                                    animal = nombreAnimal(v.animalId),
                                    onClick = { onSelect(v) },
                                    onDelete = { onDelete(v) }
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
private fun VacunasCountCard(total: Int) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Vacunas Registradas", fontSize = 12.sp, color = TextSecondary)
            Text(
                total.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Orange
            )
        }
    }
}

@Composable
private fun VacunaCard(
    v: Vacuna,
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
            Text("💉", fontSize = 34.sp)
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    v.tipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(animal, fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aplicada:", fontSize = 11.sp, color = TextSecondary)
                    Text(v.fechaAplicacion, fontSize = 11.sp, color = TextPrimary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Próxima:", fontSize = 11.sp, color = TextSecondary)
                    Text(v.proximaFecha.ifEmpty { "—" }, fontSize = 11.sp, color = TextPrimary)
                }
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

// ---------------------------------------------------------------- DIÁLOGO

@Composable
private fun VacunaDialog(
    vacuna: Vacuna? = null,
    animales: List<Animal>,
    onClose: () -> Unit,
    onSave: (Vacuna) -> Unit
) {
    val esNueva = vacuna == null
    var editando by remember { mutableStateOf(esNueva) }

    var tipo by remember { mutableStateOf(vacuna?.tipo ?: "") }
    var fecha by remember { mutableStateOf(vacuna?.fechaAplicacion ?: todayStr()) }
    var proxima by remember { mutableStateOf(vacuna?.proximaFecha ?: "") }
    var animalId by remember { mutableStateOf(vacuna?.animalId ?: 0) }

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
                        esNueva -> "Registrar Vacuna"
                        editando -> "Editar Vacuna"
                        else -> "Detalle de Vacuna"
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
                        CampoForm("Tipo de vacuna", tipo, "Tipo de vacuna") { tipo = it }
                        DatePickerField("Fecha", fecha, onFechaChange = { fecha = it })
                        DatePickerField("Próxima dosis", proxima, onFechaChange = { proxima = it })

                        Etiqueta("Animal")
                        SelectorAnimal(
                            seleccionado = nombreAnimal(animalId),
                            animales = animales,
                            onSelect = { animalId = it }
                        )
                    } else {
                        DatoItem("Tipo de vacuna", tipo)
                        DatoItem("Fecha de aplicación", fecha)
                        DatoItem("Próxima dosis", proxima)
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
                            } else if (tipo.isNotBlank()) {
                                onSave(
                                    vacuna?.copy(
                                        animalId = animalId,
                                        tipo = tipo.trim(),
                                        fechaAplicacion = fecha,
                                        proximaFecha = proxima
                                    ) ?: Vacuna(
                                        animalId = animalId,
                                        tipo = tipo.trim(),
                                        fechaAplicacion = fecha,
                                        proximaFecha = proxima
                                    )
                                )
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
