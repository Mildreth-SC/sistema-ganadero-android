package com.ganadero.control.ui.animales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.data.Animal
import com.ganadero.control.data.GanaderoRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GESTIÓN DE ESTADO REACTIVO (UiState):
 * Clase sellada que define los estados inmutables para la pantalla de Animales.
 */
sealed interface AnimalesUiState {
    object Loading : AnimalesUiState
    data class Success(
        val animales: List<Animal>,
        val animalPorEliminar: Animal? = null
    ) : AnimalesUiState
    data class Error(val message: String) : AnimalesUiState
}

/**
 * EVENTOS PUNTUALES (ONE-OFF EVENTS):
 * Clase sellada para propagar eventos unicos como Toasts o mensajes de confirmacion hacia la UI.
 */
sealed interface AnimalesEvent {
    data class ShowToast(val message: String) : AnimalesEvent
    data class ShowSuccess(val message: String) : AnimalesEvent
}

/**
 * MVVM & HILT DI:
 * ViewModel inyectado mediante Hilt (@HiltViewModel y @Inject constructor).
 */
@HiltViewModel
class AnimalesViewModel @Inject constructor(
    private val repository: GanaderoRepository
) : ViewModel() {

    private val animalPorEliminar = MutableStateFlow<Animal?>(null)

    val uiState: StateFlow<AnimalesUiState> = combine(
        repository.animales,
        animalPorEliminar
    ) { animales, porEliminar ->
        AnimalesUiState.Success(animales, porEliminar) as AnimalesUiState
    }.catch { e ->
        emit(AnimalesUiState.Error(e.message ?: "Error al cargar los animales"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnimalesUiState.Loading
    )

    private val _uiEvent = MutableSharedFlow<AnimalesEvent>()
    val uiEvent: SharedFlow<AnimalesEvent> = _uiEvent.asSharedFlow()

    fun addAnimal(a: Animal) {
        viewModelScope.launch {
            try {
                repository.addAnimal(a)
                _uiEvent.emit(AnimalesEvent.ShowSuccess("Animal registrado exitosamente"))
            } catch (e: Exception) {
                _uiEvent.emit(AnimalesEvent.ShowToast("Error al agregar animal"))
            }
        }
    }

    fun updateAnimal(a: Animal) {
        viewModelScope.launch {
            try {
                repository.updateAnimal(a)
                _uiEvent.emit(AnimalesEvent.ShowSuccess("Animal actualizado"))
            } catch (e: Exception) {
                _uiEvent.emit(AnimalesEvent.ShowToast("Error al actualizar animal"))
            }
        }
    }

    fun getAnimal(id: Int): Flow<Animal?> = repository.getAnimal(id)

    fun eliminarAnimal(a: Animal) {
        viewModelScope.launch {
            try {
                repository.deleteAnimal(a.id)
                _uiEvent.emit(AnimalesEvent.ShowSuccess("Eliminado de forma exitosa"))
            } catch (e: Exception) {
                _uiEvent.emit(AnimalesEvent.ShowToast("Error al eliminar animal"))
            }
        }
    }

    fun pedirEliminar(a: Animal) {
        animalPorEliminar.value = a
    }

    fun cancelarEliminar() {
        animalPorEliminar.value = null
    }

    fun confirmarEliminar() {
        animalPorEliminar.value?.let { animal ->
            viewModelScope.launch {
                try {
                    repository.deleteAnimal(animal.id)
                    animalPorEliminar.value = null
                    _uiEvent.emit(AnimalesEvent.ShowSuccess("Eliminado de forma exitosa"))
                } catch (e: Exception) {
                    _uiEvent.emit(AnimalesEvent.ShowToast("Error al eliminar animal"))
                }
            }
        }
    }
}
