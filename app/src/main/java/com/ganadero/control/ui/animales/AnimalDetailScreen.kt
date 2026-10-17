package com.ganadero.control.ui.animales

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadero.control.data.Animal
import com.ganadero.control.ui.components.ConfirmDialog
import com.ganadero.control.ui.components.SuccessDialog
import com.ganadero.control.ui.theme.Brown
import com.ganadero.control.ui.theme.CardBg
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary
import kotlinx.coroutines.flow.onEach

@Composable
fun AnimalDetailScreen(
    viewModel: AnimalesViewModel,
    animalId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AnimalesEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AnimalesEvent.ShowSuccess ->
                    deletedMessage = event.message
            }
        }
    }

    var loaded by remember(animalId) { mutableStateOf(false) }
    val animal by remember(animalId) {
        viewModel.getAnimal(animalId).onEach { loaded = true }
    }.collectAsStateWithLifecycle(initialValue = null)

    when {
        !loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        animal != null -> AnimalDetalleContent(
            animal = animal!!,
            onEdit = { showEdit = true },
            onDelete = { showDeleteConfirm = true }
        )
        deletedMessage == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Animal no encontrado", color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Volver", color = Color.White)
                }
            }
        }
    }

    if (showEdit && animal != null) {
        AnimalDialog(
            animal = animal,
            onClose = { showEdit = false },
            onSave = {
                viewModel.updateAnimal(it)
                showEdit = false
            }
        )
    }

    if (showDeleteConfirm && animal != null) {
        val a = animal!!
        ConfirmDialog(
            titulo = "Eliminar animal",
            mensaje = "¿Estás seguro de eliminar el animal ${a.codigo}${if (a.nombre.isNotEmpty()) " (${a.nombre})" else ""}? Se eliminarán también sus registros asociados.",
            onConfirm = {
                viewModel.eliminarAnimal(a)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    deletedMessage?.let { message ->
        SuccessDialog(
            mensaje = message,
            onDismiss = {
                deletedMessage = null
                onBack()
            }
        )
    }
}

@Composable
private fun AnimalDetalleContent(
    animal: Animal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FotoAnimal(
                    animal.fotoUri,
                    animal.sexo,
                    Modifier.size(78.dp).clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${animal.codigo.ifEmpty { "Sin código" }}" +
                            if (animal.nombre.isNotEmpty()) "-${animal.nombre}" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    EstadoBadge(animal.activo)
                }
            }
        }

        DetalleSeccion("Información") {
            DatoFila("Raza", animal.raza.ifBlank { "—" })
            DatoFila("Tipo", animal.tipo)
            DatoFila("Sexo", animal.sexo)
            DatoFila("Peso actual", "${animal.pesoActual} kg")
            DatoFila("Fecha de nacimiento", animal.fechaNacimiento.ifBlank { "—" })
            DatoFila("Estado", if (animal.activo) "Activo" else "Inactivo")
        }

        DetalleSeccion("Genealogía") {
            DatoFila("Padre", animal.padreNombre.ifBlank { "—" })
            DatoFila("Raza del padre", animal.padreRaza.ifBlank { "—" })
            DatoFila("Madre", animal.madreNombre.ifBlank { "—" })
            DatoFila("Raza de la madre", animal.madreRaza.ifBlank { "—" })
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Editar", color = Color.White)
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Danger),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Eliminar", color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetalleSeccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            contenido()
        }
    }
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, fontSize = 13.sp, color = TextSecondary)
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
