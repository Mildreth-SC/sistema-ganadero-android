package com.ganadero.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.ganadero.control.data.Repo
import com.ganadero.control.ui.AnimalesScreen
import com.ganadero.control.ui.DashboardScreen
import com.ganadero.control.ui.FinanzasScreen
import com.ganadero.control.ui.ProduccionScreen
import com.ganadero.control.ui.VacunasScreen
import com.ganadero.control.ui.theme.GanaderoTheme
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.OrangePale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repo.init(applicationContext)
        setContent {
            GanaderoTheme {
                AppRoot()
            }
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val tabs = listOf(
        TabItem("Inicio", Icons.Filled.Dashboard),
        TabItem("Animales", Icons.Filled.Pets),
        TabItem("Leche", Icons.Filled.WaterDrop),
        TabItem("Vacunas", Icons.Filled.MedicalServices),
        TabItem("Finanzas", Icons.Filled.AttachMoney)
    )
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[tab].label, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Orange,
                            selectedTextColor = Orange,
                            indicatorColor = OrangePale
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> DashboardScreen()
                1 -> AnimalesScreen()
                2 -> ProduccionScreen()
                3 -> VacunasScreen()
                else -> FinanzasScreen()
            }
        }
    }
}
