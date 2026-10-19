package com.ganadero.control.ui.produccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.model.Animal
import com.ganadero.control.data.repository.AnimalRepository
import com.ganadero.control.data.repository.ProduccionRepository
import com.ganadero.control.domain.usecase.RegistrarProduccionUseCase
import com.ganadero.control.model.Produccion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProduccionUiState {
    object Loading : ProduccionUiState
    data class Success(
        val producciones: List<Produccion>,
        val animales: List<Animal>,
        val porEliminar: Produccion? = null
    ) : ProduccionUiState
    data class Error(val message: String) : ProduccionUiState
}

sealed interface ProduccionEvent {
    data class ShowToast(val message: String) : ProduccionEvent
    data class ShowSuccess(val message: String) : ProduccionEvent
}

/**
 * MVVM & HILT DI:
 * Combina ProduccionRepository y AnimalRepository (para el selector de animal);
 * el alta se delega al caso de uso RegistrarProduccionUseCase.
 */
@HiltViewModel
class ProduccionViewModel @Inject constructor(
    private val produccionRepository: ProduccionRepository,
    private val animalRepository: AnimalRepository,
    private val registrarProduccion: RegistrarProduccionUseCase
) : ViewModel() {

    private val porEliminar = MutableStateFlow<Produccion?>(null)

    val uiState: StateFlow<ProduccionUiState> = combine(
        produccionRepository.producciones,
        animalRepository.animales,
        porEliminar
    ) { producciones, animales, aEliminar ->
        ProduccionUiState.Success(producciones, animales, aEliminar) as ProduccionUiState
    }.catch { e ->
        emit(ProduccionUiState.Error(e.message ?: "Error al cargar la producción"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProduccionUiState.Loading
    )

    private val _uiEvent = MutableSharedFlow<ProduccionEvent>()
    val uiEvent: SharedFlow<ProduccionEvent> = _uiEvent.asSharedFlow()

    fun addProduccion(p: Produccion) {
        viewModelScope.launch {
            try {
                registrarProduccion(p)
                _uiEvent.emit(ProduccionEvent.ShowSuccess("Producción registrada"))
            } catch (e: Exception) {
                _uiEvent.emit(ProduccionEvent.ShowToast(e.message ?: "Error al registrar producción"))
            }
        }
    }

    fun updateProduccion(p: Produccion) {
        viewModelScope.launch {
            try {
                produccionRepository.updateProduccion(p)
                _uiEvent.emit(ProduccionEvent.ShowSuccess("Registro actualizado"))
            } catch (e: Exception) {
                _uiEvent.emit(ProduccionEvent.ShowToast("Error al actualizar registro"))
            }
        }
    }

    fun pedirEliminar(p: Produccion) { porEliminar.value = p }
    fun cancelarEliminar() { porEliminar.value = null }

    fun confirmarEliminar() {
        val p = porEliminar.value ?: return
        viewModelScope.launch {
            try {
                produccionRepository.deleteProduccion(p.id)
                porEliminar.value = null
                _uiEvent.emit(ProduccionEvent.ShowSuccess("Eliminado de forma exitosa"))
            } catch (e: Exception) {
                _uiEvent.emit(ProduccionEvent.ShowToast("Error al eliminar registro"))
            }
        }
    }
}
