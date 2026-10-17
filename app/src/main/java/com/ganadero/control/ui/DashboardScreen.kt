package com.ganadero.control.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganadero.control.data.Repo
import com.ganadero.control.ui.theme.Danger
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.Success
import com.ganadero.control.ui.theme.TextSecondary

@Composable
fun DashboardScreen() {
    val hoy = todayStr()
    val mes = hoy.substring(0, 7)

    val activos = Repo.animales.count { it.estado == "Activo" }
    val litrosHoy = Repo.producciones.filter { it.fecha == hoy }.sumOf { it.litros }
    val vacunasPend = Repo.vacunas.count { it.proximaFecha.isNotEmpty() }
    val balance = Repo.finanzas.filter { it.fecha.startsWith(mes) }
        .sumOf { if (it.tipo == "Ingreso") it.monto else -it.monto }

    val days = last7Days()
    val litrosPorDia = days.map { d -> Repo.producciones.filter { it.fecha == d.first }.sumOf { it.litros } }
    val max = (litrosPorDia.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Animales activos", activos.toString(), accent = Orange, modifier = Modifier.weight(1f))
            StatCard("Leche hoy", "${oneDecimal(litrosHoy)} L", accent = Orange, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Vacunas", vacunasPend.toString(), accent = Orange, modifier = Modifier.weight(1f))
            StatCard(
                "Balance mes",
                money(balance),
                accent = if (balance >= 0) Success else Danger,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Producción últimos 7 días", fontWeight = FontWeight.Bold, color = com.ganadero.control.ui.theme.TextPrimary)
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().height(150.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEachIndexed { i, d ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            val h = ((litrosPorDia[i] / max) * 110).dp
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .height(if (h < 4.dp) 4.dp else h)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(Orange)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(d.second, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Resumen del hato", fontWeight = FontWeight.Bold, color = com.ganadero.control.ui.theme.TextPrimary)
                ResumenRow("Total de animales", Repo.animales.size.toString())
                ResumenRow("Hembras", Repo.animales.count { it.sexo == "Hembra" }.toString())
                ResumenRow("Machos", Repo.animales.count { it.sexo == "Macho" }.toString())
                ResumenRow("Producción acumulada", "${oneDecimal(Repo.producciones.sumOf { it.litros })} L")
            }
        }
    }
}

@Composable
private fun ResumenRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, color = com.ganadero.control.ui.theme.TextPrimary, fontSize = 14.sp)
    }
}
