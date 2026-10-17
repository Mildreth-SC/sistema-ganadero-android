package com.ganadero.control.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

val BrownColor = Color(0xFF7A2E10)
val DialogBgColor = Color(0xFFF1EBE8)

@Composable
fun Etiqueta(texto: String) {
    Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
}

@Composable
fun CampoForm(
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
            color = BrownColor,
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
                focusedBorderColor = BrownColor,
                unfocusedBorderColor = BrownColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = BrownColor
            ),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )
    }
}

@Composable
fun PillBtn(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .width(110.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(if (seleccionado) BrownColor else Color.Transparent)
            .border(1.dp, BrownColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = if (seleccionado) Color.White else BrownColor
        )
    }
}

@Composable
fun DatoItem(etiqueta: String, valor: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(etiqueta, fontSize = 12.sp, color = TextSecondary)
        Text(
            valor.ifBlank { "—" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}