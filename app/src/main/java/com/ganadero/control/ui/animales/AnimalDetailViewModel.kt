package com.ganadero.control.ui.animales

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.model.Animal
import com.ganadero.control.data.repository.GanaderoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ESTADO DE LA PANTALLA DE DETALLE (UiState):
 * Clase sellada que representa los estados inmutables del detalle de un animal.
 */
sealed interface AnimalDetailUiState {
    object Loading : AnimalDetailUiState
    data class Success(
        val animal: Animal,
        val producciones: List<com.ganadero.control.model.Produccion>
    ) : AnimalDetailUiState
    object NotFound : AnimalDetailUiState
    data class Error(val message: String) : AnimalDetailUiState
}

/**
 * EVENTOS PUNTUALES (ONE-OFF EVENTS):
 * Clase sellada para mensajes unicos (Toast o confirmacion de exito) hacia la UI.
 */
sealed interface AnimalDetailEvent {
    data class ShowToast(val message: String) : AnimalDetailEvent
    data class ShowSuccess(val message: String) : AnimalDetailEvent
}

/**
 * VIEWMODEL DEL DETALLE (MVVM + HILT + SavedStateHandle):
 * Obtiene el argumento `animalId` directamente desde el SavedStateHandle que
 * Hilt inyecta a partir de los argumentos de la ruta de navegacion
 * (animal_detail/{animalId}). No depende de parametros de la UI.
 */
@HiltViewModel
class AnimalDetailViewModel @Inject constructor(
    private val repository: GanaderoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val animalId: Int = savedStateHandle.get<Int>("animalId") ?: 0

    val uiState: StateFlow<AnimalDetailUiState> = combine(
        repository.getAnimal(animalId),
        repository.getProduccionesByAnimal(animalId)
    ) { animal, producciones ->
        if (animal == null) AnimalDetailUiState.NotFound
        else AnimalDetailUiState.Success(animal, producciones)
    }.catch { e ->
        emit(AnimalDetailUiState.Error(e.message ?: "Error al cargar el animal"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnimalDetailUiState.Loading
    )

    private val _uiEvent = MutableSharedFlow<AnimalDetailEvent>()
    val uiEvent: SharedFlow<AnimalDetailEvent> = _uiEvent.asSharedFlow()

    fun updateAnimal(a: Animal) {
        viewModelScope.launch {
            try {
                repository.updateAnimal(a)
                _uiEvent.emit(AnimalDetailEvent.ShowSuccess("Animal actualizado"))
            } catch (e: Exception) {
                _uiEvent.emit(AnimalDetailEvent.ShowToast("Error al actualizar animal"))
            }
        }
    }

    fun deleteAnimal(a: Animal) {
        viewModelScope.launch {
            try {
                repository.deleteAnimal(a.id)
                _uiEvent.emit(AnimalDetailEvent.ShowSuccess("Eliminado de forma exitosa"))
            } catch (e: Exception) {
                _uiEvent.emit(AnimalDetailEvent.ShowToast("Error al eliminar animal"))
            }
        }
    }
}
