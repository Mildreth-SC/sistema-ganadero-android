package com.ganadero.control.ui.finanzas

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.ganadero.control.data.Finanza
import com.ganadero.control.ui.components.BrownColor
import com.ganadero.control.ui.components.CampoForm
import com.ganadero.control.ui.components.ConfirmDialog
import com.ganadero.control.ui.components.DatePickerField
import com.ganadero.control.ui.components.DatoItem
import com.ganadero.control.ui.components.DialogBgColor
import com.ganadero.control.ui.components.Etiqueta
import com.ganadero.control.ui.components.PillBtn
import com.ganadero.control.ui.components.SuccessDialog
import com.ganadero.control.ui.money
import com.ganadero.control.ui.todayStr
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.Success
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

private val CardBg = Color(0xFFFDF7F3)

@Composable
fun FinanzasScreen(viewModel: FinanzasViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf<Finanza?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FinanzasEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is FinanzasEvent.ShowSuccess ->
                    successMessage = event.message
            }
        }
    }

    FinanzasContent(
        state = uiState,
        onAdd = { showDialog = true },
        onSelect = { seleccionado = it },
        onDelete = viewModel::pedirEliminar
    )

    if (showDialog) {
        FinanzaDialog(
            onClose = { showDialog = false },
            onSave = {
                viewModel.addFinanza(it)
                showDialog = false
            }
        )
    }

    seleccionado?.let { f ->
        FinanzaDialog(
            finanza = f,
            onClose = { seleccionado = null },
            onSave = {
                viewModel.updateFinanza(it)
                seleccionado = null
            }
        )
    }

    (uiState as? FinanzasUiState.Success)?.porEliminar?.let { f ->
        ConfirmDialog(
            titulo = "Eliminar",
            mensaje = "¿Estás seguro de eliminar el movimiento \"${f.categoria.ifEmpty { f.tipo }}\" por ${money(f.monto)}?",
            onConfirm = viewModel::confirmarEliminar,
            onDismiss = viewModel::cancelarEliminar
        )
    }

    successMessage?.let { message ->
        SuccessDialog(mensaje = message, onDismiss = { successMessage = null })
    }
}

@Composable
private fun FinanzasContent(
    state: FinanzasUiState,
    onAdd: () -> Unit,
    onSelect: (Finanza) -> Unit,
    onDelete: (Finanza) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (state) {
            is FinanzasUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Orange)
            }
            is FinanzasUiState.Error -> {
                Text(state.message, Modifier.align(Alignment.Center), color = Danger)
            }
            is FinanzasUiState.Success -> {
                val movimientos = state.finanzas
                val ingresos = movimientos.filter { it.tipo == "Ingreso" }.sumOf { it.monto }
                val gastos = movimientos.filter { it.tipo == "Gasto" }.sumOf { it.monto }
                val balance = ingresos - gastos

                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResumenCard("Ingresos", money(ingresos), Success, Modifier.weight(1f))
                        ResumenCard("Gastos", money(gastos), Danger, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        ResumenCard(
                            "Balance",
                            money(balance),
                            if (balance >= 0) Success else Danger,
                            Modifier.fillMaxWidth(0.48f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    if (movimientos.isEmpty()) {
                        Text(
                            "Sin movimientos registrados",
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(movimientos.sortedByDescending { it.fecha }, key = { it.id }) { f ->
                                FinanzaCard(
                                    f = f,
                                    onClick = { onSelect(f) },
                                    onDelete = { onDelete(f) }
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
private fun ResumenCard(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = color)
            Text(titulo, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun FinanzaCard(f: Finanza, onClick: () -> Unit, onDelete: () -> Unit) {
    val esIngreso = f.tipo == "Ingreso"
    val colorMonto = if (esIngreso) Success else Danger

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
            Text(if (esIngreso) "📈" else "📉", fontSize = 34.sp)
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        f.categoria.ifEmpty { "General" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        (if (esIngreso) "+" else "-") + money(f.monto),
                        fontSize = 14.sp,
                        color = colorMonto
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(f.categoria, fontSize = 12.sp, color = TextSecondary)
                    Text("•", fontSize = 12.sp, color = TextSecondary)
                    Text(f.fecha, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.width(10.dp))
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
private fun FinanzaDialog(
    finanza: Finanza? = null,
    onClose: () -> Unit,
    onSave: (Finanza) -> Unit
) {
    val esNuevo = finanza == null
    var editando by remember { mutableStateOf(esNuevo) }

    var tipo by remember { mutableStateOf(finanza?.tipo ?: "Ingreso") }
    var categoria by remember { mutableStateOf(finanza?.categoria ?: "") }
    var monto by remember { mutableStateOf(finanza?.monto?.toString() ?: "") }
    var fecha by remember { mutableStateOf(finanza?.fecha ?: todayStr()) }
    var descripcion by remember { mutableStateOf(finanza?.descripcion ?: "") }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DialogBgColor,
            modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = 640.dp)
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(
                    when {
                        esNuevo -> "Nuevo Movimiento"
                        editando -> "Editar Movimiento"
                        else -> "Detalle del Movimiento"
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
                        Etiqueta("Tipo")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PillBtn("Ingreso", tipo == "Ingreso") { tipo = "Ingreso" }
                            PillBtn("Gasto", tipo == "Gasto") { tipo = "Gasto" }
                        }
                        CampoForm("Categoría", categoria, "Categoría") { categoria = it }
                        CampoForm("Monto", monto, "$", KeyboardType.Decimal) { monto = it }
                        DatePickerField("Fecha", fecha, onFechaChange = { fecha = it })
                        DescripcionCampo(descripcion) { descripcion = it }
                    } else {
                        DatoItem("Tipo", tipo)
                        DatoItem("Categoría", categoria)
                        DatoItem("Monto", money(finanza?.monto ?: 0.0))
                        DatoItem("Fecha", fecha)
                        DatoItem("Descripción", descripcion)
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
                                val m = monto.toDoubleOrNull()
                                if (m != null) {
                                    onSave(
                                        finanza?.copy(
                                            fecha = fecha,
                                            tipo = tipo,
                                            categoria = categoria.trim(),
                                            monto = m,
                                            descripcion = descripcion.trim()
                                        ) ?: Finanza(
                                            fecha = fecha,
                                            tipo = tipo,
                                            categoria = categoria.trim(),
                                            monto = m,
                                            descripcion = descripcion.trim()
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

@Composable
private fun DescripcionCampo(valor: String, onCambio: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Descripción", fontSize = 13.sp, color = BrownColor)
            Spacer(Modifier.width(6.dp))
            Text("Opcional", fontSize = 9.sp, color = TextSecondary)
        }
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text("Descripción", fontSize = 12.sp, color = TextSecondary) },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrownColor,
                unfocusedBorderColor = BrownColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = BrownColor
            ),
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}