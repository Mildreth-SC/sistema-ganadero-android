package com.ganadero.control.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganadero.control.ui.animales.AnimalDetailScreen
import com.ganadero.control.ui.animales.AnimalesScreen
import com.ganadero.control.ui.dashboard.DashboardScreen
import com.ganadero.control.ui.finanzas.FinanzasScreen
import com.ganadero.control.ui.produccion.ProduccionScreen
import com.ganadero.control.ui.vacunas.VacunasScreen

/**
 * NAVEGACIÓN (NAVHOST):
 * Grafo de navegación central que administra la navegación entre pantallas mediante NavHost y NavController,
 * garantizando el paso seguro de argumentos (e.g. animalId).
 */
@Composable
fun GanaderoNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel = hiltViewModel())
        }
        composable(Screen.Animales.route) {
            AnimalesScreen(
                viewModel = hiltViewModel(),
                onAnimalClick = { animalId ->
                    navController.navigate(Screen.AnimalDetail.createRoute(animalId))
                }
            )
        }
        composable(Screen.Produccion.route) {
            ProduccionScreen(viewModel = hiltViewModel())
        }
        composable(Screen.Vacunas.route) {
            VacunasScreen(viewModel = hiltViewModel())
        }
        composable(Screen.Finanzas.route) {
            FinanzasScreen(viewModel = hiltViewModel())
        }
        // NAVEGACIÓN CON ARGUMENTOS: Definición de ruta con argumento seguro tipo Int (animalId)
        composable(
            route = Screen.AnimalDetail.route,
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0
            AnimalDetailScreen(
                viewModel = hiltViewModel(),
                animalId = animalId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
