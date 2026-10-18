package com.ganadero.control.ui.animales

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ganadero.control.data.Animal
import com.ganadero.control.ui.components.ConfirmDialog
import com.ganadero.control.ui.components.SuccessDialog
import com.ganadero.control.ui.theme.*

@Composable
fun AnimalesScreen(
    viewModel: AnimalesViewModel,
    onAnimalClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var animalToEdit by remember { mutableStateOf<Animal?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AnimalesEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AnimalesEvent.ShowSuccess ->
                    successMessage = event.message
            }
        }
    }

    AnimalesContent(
        state = uiState,
        onAdd = { showDialog = true },
        onAnimalClick = onAnimalClick,
        onEdit = { animalToEdit = it },
        onDelete = viewModel::pedirEliminar
    )

    if (showDialog) {
        AnimalDialog(
            onClose = { showDialog = false },
            onSave = {
                viewModel.addAnimal(it)
                showDialog = false
            }
        )
    }

    animalToEdit?.let { animal ->
        AnimalDialog(
            animal = animal,
            onClose = { animalToEdit = null },
            onSave = {
                viewModel.updateAnimal(it)
                animalToEdit = null
            }
        )
    }

    (uiState as? AnimalesUiState.Success)?.animalPorEliminar?.let { animal ->
        ConfirmDialog(
            titulo = "Eliminar animal",
            mensaje = "¿Estás seguro de eliminar el animal ${animal.codigo}${if (animal.nombre.isNotEmpty()) " (${animal.nombre})" else ""}? Se eliminarán también sus registros asociados.",
            onConfirm = viewModel::confirmarEliminar,
            onDismiss = viewModel::cancelarEliminar
        )
    }

    successMessage?.let { message ->
        SuccessDialog(mensaje = message, onDismiss = { successMessage = null })
    }
}

@Composable
private fun AnimalesContent(
    state: AnimalesUiState,
    onAdd: () -> Unit,
    onAnimalClick: (Int) -> Unit,
    onEdit: (Animal) -> Unit,
    onDelete: (Animal) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (state) {
            is AnimalesUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Orange)
            }
            is AnimalesUiState.Error -> {
                Text(state.message, Modifier.align(Alignment.Center), color = Danger)
            }
            is AnimalesUiState.Success -> {
                val animales = state.animales
                if (animales.isEmpty()) {
                    Text(
                        "No hay animales registrados",
                        Modifier.align(Alignment.Center),
                        color = TextSecondary
                    )
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(animales, key = { it.id }) { a ->
                            AnimalCard(
                                a = a,
                                onClick = { onAnimalClick(a.id) },
                                onEdit = { onEdit(a) },
                                onDelete = { onDelete(a) }
                            )
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

// ------------------- TARJETA

@Composable
private fun AnimalCard(
    a: Animal,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoAnimal(a.fotoUri, a.sexo, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "${a.codigo}${if (a.nombre.isNotEmpty()) "-${a.nombre}" else ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    "${a.raza.ifEmpty { "Raza" }} - ${a.tipo} - ${a.pesoActual} kg",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Madre: ${a.madreNombre.ifEmpty { "—" }}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        "Padre: ${a.padreNombre.ifEmpty { "—" }}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(
                Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                EstadoBadge(a.activo)
                Box(
                    Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Orange.copy(alpha = 0.2f))
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = Orange,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Orange.copy(alpha = 0.2f))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = Orange,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun EstadoBadge(activo: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (activo) ActivoBg else InactivoBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            if (activo) "Activo" else "Inactivo",
            fontSize = 10.sp,
            color = if (activo) ActivoText else InactivoText
        )
    }
}

@Composable
internal fun FotoAnimal(uri: String?, sexo: String, modifier: Modifier) {
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = "Foto del animal",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier.background(Color(0xFFEDE3DD)), contentAlignment = Alignment.Center) {
            Text(if (sexo == "Macho") "🐂" else "🐄", fontSize = 30.sp)
        }
    }
}

// ------------------------------- DIÁLOGO

@Composable
internal fun AnimalDialog(
    animal: Animal? = null,
    onClose: () -> Unit,
    onSave: (Animal) -> Unit
) {
    val context = LocalContext.current
    val esNuevo = animal == null

    var foto by remember { mutableStateOf(animal?.fotoUri) }
    var codigo by remember { mutableStateOf(animal?.codigo ?: "") }
    var nombre by remember { mutableStateOf(animal?.nombre ?: "") }
    var raza by remember { mutableStateOf(animal?.raza ?: "") }
    var peso by remember { mutableStateOf(animal?.pesoActual?.toString() ?: "") }
    var tipo by remember { mutableStateOf(animal?.tipo ?: "Leche") }
    var sexo by remember { mutableStateOf(animal?.sexo ?: "Hembra") }
    var padre by remember { mutableStateOf(animal?.padreNombre ?: "") }
    var madre by remember { mutableStateOf(animal?.madreNombre ?: "") }
    var activo by remember { mutableStateOf(animal?.activo ?: true) }
    var estadoRepro by remember { mutableStateOf(animal?.estadoReproductivo ?: "Ideal") }

    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            foto = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DialogBg,
            modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = 680.dp)
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Foto
                    Box(
                        Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCCFC7))
                            .border(1.dp, Brown, CircleShape)
                            .clickable { selectorFoto.launch(arrayOf("image/*")) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (foto != null) {
                            AsyncImage(
                                model = foto,
                                contentDescription = "Foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("🖼", fontSize = 28.sp)
                        }
                    }
                    Text(
                        if (foto == null) "Agregar foto" else "Cambiar foto",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CampoForm("Codigo/Arete", codigo, "Codigo/Arete") { codigo = it }
                            CampoForm("Nombre", nombre, "Nombre") { nombre = it }
                            CampoForm("Raza", raza, "Raza") { raza = it }
                            CampoForm("Peso", peso, "Peso (kg)", KeyboardType.Decimal) { peso = it }

                            Etiqueta("Sexo")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                PillBtn("Hembra", sexo == "Hembra") { sexo = "Hembra" }
                                PillBtn("Macho", sexo == "Macho") { sexo = "Macho" }
                            }

                            Etiqueta("Tipo")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                PillBtn("Carne", tipo == "Carne") { tipo = "Carne" }
                                PillBtn("Leche", tipo == "Leche") { tipo = "Leche" }
                            }

                            if (!esNuevo) {
                                Etiqueta("Estado")
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PillBtn("Activo", activo) { activo = true }
                                    PillBtn("Inactivo", !activo) { activo = false }
                                }
                            }

                            Etiqueta("Estado reproductivo")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PillBtn("Ideal", estadoRepro == "Ideal") { estadoRepro = "Ideal" }
                                PillBtn("En gestión", estadoRepro == "En gestión") { estadoRepro = "En gestión" }
                                PillBtn("Descarte", estadoRepro == "Descarte") { estadoRepro = "Descarte" }
                            }

                            Text(
                                "Genealogía",
                                fontSize = 15.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            CampoForm("Padre", padre, "Nombre del padre") { padre = it }
                            CampoForm("Madre", madre, "Nombre de la madre") { madre = it }
                        }
                }

                // Botones
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onClose) {
                        Text(
                            "Cancelar",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (codigo.isNotBlank()) {
                                val pesoNum = peso.toDoubleOrNull() ?: animal?.pesoActual ?: 0.0
                                onSave(
                                    animal?.copy(
                                        codigo = codigo.trim(),
                                        nombre = nombre.trim(),
                                        raza = raza.trim(),
                                        pesoActual = pesoNum,
                                        tipo = tipo,
                                        sexo = sexo,
                                        madreNombre = madre.trim(),
                                        padreNombre = padre.trim(),
                                        fotoUri = foto,
                                        activo = activo,
                                        estadoReproductivo = estadoRepro
                                    ) ?: Animal(
                                        codigo = codigo.trim(),
                                        nombre = nombre.trim(),
                                        raza = raza.trim(),
                                        pesoActual = pesoNum,
                                        tipo = tipo,
                                        sexo = sexo,
                                        madreNombre = madre.trim(),
                                        padreNombre = padre.trim(),
                                        fotoUri = foto,
                                        activo = true,
                                        estadoReproductivo = estadoRepro
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = CircleShape,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

// ----------------------------------- COMPONENTES

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
}

@Composable
private fun CampoForm(
    etiqueta: String,
    valor: String,
    placeholder: String,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            etiqueta,
            fontSize = 13.sp,
            color = Brown,
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder, fontSize = 12.sp, color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brown,
                unfocusedBorderColor = Brown,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Brown
            ),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )
    }
}

@Composable
private fun PillBtn(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .width(110.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(if (seleccionado) Brown else Color.Transparent)
            .border(1.dp, Brown, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = if (seleccionado) Color.White else Brown
        )
    }
}

