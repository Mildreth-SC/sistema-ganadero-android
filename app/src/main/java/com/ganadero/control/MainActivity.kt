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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ganadero.control.ui.navigation.GanaderoNavGraph
import com.ganadero.control.ui.navigation.Screen
import com.ganadero.control.ui.theme.GanaderoTheme
import com.ganadero.control.ui.theme.Orange
import com.ganadero.control.ui.theme.OrangePale
import dagger.hilt.android.AndroidEntryPoint

/**
 * INYECCIÓN DE DEPENDENCIAS (HILT):
 * Actividad principal anotada con @AndroidEntryPoint para habilitar la inyección de dependencias
 * en componentes de la interfaz de usuario.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GanaderoTheme {
                AppRoot()
            }
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector, val screen: Screen)

/**
 * NAVEGACIÓN Y ESTRUCTURA DE LA APLICACIÓN:
 * Contenedor raíz que integra NavController, la barra de navegación inferior (NavigationBar)
 * y el grafo de navegación (GanaderoNavGraph).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    // NAVEGACIÓN: Inicialización del NavController
    val navController = rememberNavController()

    val tabs = listOf(
        TabItem("Inicio", Icons.Filled.Dashboard, Screen.Dashboard),
        TabItem("Animales", Icons.Filled.Pets, Screen.Animales),
        TabItem("Leche", Icons.Filled.WaterDrop, Screen.Produccion),
        TabItem("Vacunas", Icons.Filled.MedicalServices, Screen.Vacunas),
        TabItem("Finanzas", Icons.Filled.AttachMoney, Screen.Finanzas)
    )

    // NAVEGACIÓN: Suscripción a la ruta actual para actualizar el estado del NavigationBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTab = tabs.find { tab ->
        currentRoute != null && currentRoute.startsWith(tab.screen.route.split("/")[0])
    } ?: tabs.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTab.label, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEach { tabItem ->
                    val isSelected = currentRoute != null && currentRoute.startsWith(tabItem.screen.route.split("/")[0])
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            // NAVEGACIÓN: Transición entre pantallas con gestión de pila (saveState, restoreState)
                            navController.navigate(tabItem.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tabItem.icon, contentDescription = tabItem.label) },
                        label = { Text(tabItem.label) },
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
            // NAVEGACIÓN: Hosting del grafo de navegación con NavHost
            GanaderoNavGraph(navController = navController)
        }
    }
}
