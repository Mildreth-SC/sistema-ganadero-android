package com.ganadero.control.ui.animales

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.TextPrimary
import com.ganadero.control.ui.theme.TextSecondary

/**
 * GRÁFICA DE BARRAS (CANVAS):
 * Dibuja las barras de producción de leche sin librerías externas.
 * Recibe una lista de pares (etiqueta, valor) y las dibuja proporcionalmente.
 */
@Composable
fun GraficaBarras(
    datos: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    if (datos.isEmpty()) {
        Text("Sin registros de producción", fontSize = 12.sp, color = TextSecondary)
        return
    }

    val maxValue = (datos.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val totalBars = datos.size
            val totalWidth = size.width
            val totalHeight = size.height
            val barSpacing = 12f
            val barWidth = (totalWidth - barSpacing * (totalBars + 1)) / totalBars
            val labelPadding = 28f
            val chartHeight = totalHeight - labelPadding

            datos.forEachIndexed { index, (etiqueta, valor) ->
                val left = barSpacing + index * (barWidth + barSpacing)
                val barHeight = (valor / maxValue) * chartHeight
                val top = chartHeight - barHeight

                // Barra
                drawRoundRect(
                    color = Orange,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Valor encima de la barra
                drawContext.canvas.nativeCanvas.apply {
                    val valuePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#3D2C29")
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(
                        if (valor % 1f == 0f) valor.toInt().toString() else "%.1f".format(valor),
                        left + barWidth / 2,
                        top - 8f,
                        valuePaint
                    )

                    // Etiqueta debajo de la barra
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8A7A75")
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(
                        etiqueta,
                        left + barWidth / 2,
                        totalHeight - 6f,
                        labelPaint
                    )
                }
            }
        }
    }
}

@Composable
fun ResumenProduccion(totalRegistros: Int, totalLitros: Double) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Registros: $totalRegistros", fontSize = 12.sp, color = TextSecondary)
        Text(
            "Total: ${"%.1f".format(totalLitros)} L",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
