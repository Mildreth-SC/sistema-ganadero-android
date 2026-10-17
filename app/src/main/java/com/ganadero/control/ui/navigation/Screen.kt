package com.ganadero.control.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * NAVEGACIÓN:
 * Clase sellada que define las rutas, títulos e íconos para cada pantalla en el sistema de navegación.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Filled.Dashboard)
    object Animales : Screen("animales", "Animales", Icons.Filled.Pets)
    object Produccion : Screen("produccion", "Leche", Icons.Filled.WaterDrop)
    object Vacunas : Screen("vacunas", "Vacunas", Icons.Filled.MedicalServices)
    object Finanzas : Screen("finanzas", "Finanzas", Icons.Filled.AttachMoney)

    // NAVEGACIÓN: Ruta con argumento dinámico (animalId)
    object AnimalDetail : Screen("animal_detail/{animalId}", "Detalle Animal", Icons.Filled.Pets) {
        fun createRoute(animalId: Int) = "animal_detail/$animalId"
    }
}
