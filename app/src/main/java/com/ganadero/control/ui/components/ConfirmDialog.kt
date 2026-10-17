package com.ganadero.control.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

private val DialogCard = Color(0xFFFDF7F3)
private val Rojo = Color(0xFFE53935)
private val Verde = Color(0xFF2E9E5B)
private val GrisBoton = Color(0xFFDDE1E8)
private val TextoOscuro = Color(0xFF2B2B2B)
private val TextoSuave = Color(0xFF555555)

@Composable
fun ConfirmDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "Eliminar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogCard,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Rojo,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    titulo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoOscuro
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    mensaje,
                    fontSize = 12.sp,
                    color = TextoSuave,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrisBoton),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 6.dp)
                    ) {
                        Text("Cancelar", color = TextoSuave, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 26.dp, vertical = 6.dp)
                    ) {
                        Text(textoConfirmar, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(
    mensaje: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogCard,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                Modifier.padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Verde),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    mensaje,
                    fontSize = 16.sp,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}