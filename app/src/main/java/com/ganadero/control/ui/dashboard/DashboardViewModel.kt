package com.ganadero.control.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.model.Animal
import com.ganadero.control.model.Finanza
import com.ganadero.control.data.repository.GanaderoRepository
import com.ganadero.control.model.Produccion
import com.ganadero.control.model.Vacuna
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * GESTIÓN DE ESTADO REACTIVO (UiState):
 * Clase sellada que encapsula los estados de UI: Loading, Success y Error.
 */
sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val animales: List<Animal>,
        val producciones: List<Produccion>,
        val vacunas: List<Vacuna>,
        val finanzas: List<Finanza>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

/**
 * MVVM & HILT DI:
 * ViewModel inyectado mediante Hilt (@HiltViewModel y @Inject constructor).
 * Expone un único StateFlow<DashboardUiState> combinado desde el repositorio.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: GanaderoRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.animales,
        repository.producciones,
        repository.vacunas,
        repository.finanzas
    ) { animales, producciones, vacunas, finanzas ->
        DashboardUiState.Success(animales, producciones, vacunas, finanzas) as DashboardUiState
    }.catch { e ->
        emit(DashboardUiState.Error(e.message ?: "Error al cargar los datos"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )
}
