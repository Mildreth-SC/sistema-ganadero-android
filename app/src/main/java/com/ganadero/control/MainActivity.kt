package com.ganadero.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // DISEÑO ADAPTATIVO: se calcula el WindowSizeClass de la ventana actual.
            val windowSizeClass = calculateWindowSizeClass(this)
            GanaderoTheme {
                AppRoot(windowSizeClass = windowSizeClass)
            }
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector, val screen: Screen)

/**
 * NAVEGACIÓN Y ESTRUCTURA DE LA APLICACIÓN (DISEÑO ADAPTATIVO):
 * Contenedor raíz que integra NavController, el grafo de navegación (GanaderoNavGraph)
 * y la navegación principal. Según el WindowSizeClass se reorganiza el layout:
 *  - Compact (teléfono): NavigationBar inferior.
 *  - Medium/Expanded (plegable, tablet, escritorio): NavigationRail lateral.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(windowSizeClass: WindowSizeClass) {
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

    fun isSelected(route: String): Boolean =
        currentRoute != null && currentRoute.startsWith(route.split("/")[0])

    val onNavigate: (Screen) -> Unit = { screen ->
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text(currentTab.label, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Orange,
                titleContentColor = Color.White
            )
        )
    }

    // DISEÑO ADAPTATIVO: se reorganiza el layout según el ancho de ventana.
    val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (isExpanded) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(containerColor = Color.White) {
                Spacer(Modifier.height(12.dp))
                tabs.forEach { tabItem ->
                    NavigationRailItem(
                        selected = isSelected(tabItem.screen.route),
                        onClick = { onNavigate(tabItem.screen) },
                        icon = { Icon(tabItem.icon, contentDescription = tabItem.label) },
                        label = { Text(tabItem.label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Orange,
                            selectedTextColor = Orange,
                            indicatorColor = OrangePale
                        )
                    )
                }
            }
            Scaffold(modifier = Modifier.weight(1f), topBar = topBar) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    GanaderoNavGraph(navController = navController)
                }
            }
        }
    } else {
        Scaffold(
            topBar = topBar,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEach { tabItem ->
                        NavigationBarItem(
                            selected = isSelected(tabItem.screen.route),
                            onClick = { onNavigate(tabItem.screen) },
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
}
