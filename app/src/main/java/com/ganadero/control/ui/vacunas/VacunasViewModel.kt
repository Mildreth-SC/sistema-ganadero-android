package com.ganadero.control.ui.vacunas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.model.Animal
import com.ganadero.control.data.repository.AnimalRepository
import com.ganadero.control.data.repository.VacunaRepository
import com.ganadero.control.domain.usecase.RegistrarVacunaUseCase
import com.ganadero.control.model.Vacuna
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

sealed interface VacunasUiState {
    object Loading : VacunasUiState
    data class Success(
        val vacunas: List<Vacuna>,
        val animales: List<Animal>,
        val porEliminar: Vacuna? = null
    ) : VacunasUiState
    data class Error(val message: String) : VacunasUiState
}

sealed interface VacunasEvent {
    data class ShowToast(val message: String) : VacunasEvent
    data class ShowSuccess(val message: String) : VacunasEvent
}

/**
 * MVVM & HILT DI:
 * Combina VacunaRepository y AnimalRepository (selector de animal);
 * el alta se delega al caso de uso RegistrarVacunaUseCase.
 */
@HiltViewModel
class VacunasViewModel @Inject constructor(
    private val vacunaRepository: VacunaRepository,
    private val animalRepository: AnimalRepository,
    private val registrarVacuna: RegistrarVacunaUseCase
) : ViewModel() {

    private val porEliminar = MutableStateFlow<Vacuna?>(null)

    val uiState: StateFlow<VacunasUiState> = combine(
        vacunaRepository.vacunas,
        animalRepository.animales,
        porEliminar
    ) { vacunas, animales, aEliminar ->
        VacunasUiState.Success(vacunas, animales, aEliminar) as VacunasUiState
    }.catch { e ->
        emit(VacunasUiState.Error(e.message ?: "Error al cargar las vacunas"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VacunasUiState.Loading
    )

    private val _uiEvent = MutableSharedFlow<VacunasEvent>()
    val uiEvent: SharedFlow<VacunasEvent> = _uiEvent.asSharedFlow()

    fun addVacuna(v: Vacuna) {
        viewModelScope.launch {
            try {
                registrarVacuna(v)
                _uiEvent.emit(VacunasEvent.ShowSuccess("Vacuna registrada"))
            } catch (e: Exception) {
                _uiEvent.emit(VacunasEvent.ShowToast(e.message ?: "Error al registrar vacuna"))
            }
        }
    }

    fun updateVacuna(v: Vacuna) {
        viewModelScope.launch {
            try {
                vacunaRepository.updateVacuna(v)
                _uiEvent.emit(VacunasEvent.ShowSuccess("Vacuna actualizada"))
            } catch (e: Exception) {
                _uiEvent.emit(VacunasEvent.ShowToast("Error al actualizar vacuna"))
            }
        }
    }

    fun pedirEliminar(v: Vacuna) { porEliminar.value = v }
    fun cancelarEliminar() { porEliminar.value = null }

    fun confirmarEliminar() {
        val v = porEliminar.value ?: return
        viewModelScope.launch {
            try {
                vacunaRepository.deleteVacuna(v.id)
                porEliminar.value = null
                _uiEvent.emit(VacunasEvent.ShowSuccess("Eliminado de forma exitosa"))
            } catch (e: Exception) {
                _uiEvent.emit(VacunasEvent.ShowToast("Error al eliminar vacuna"))
            }
        }
    }
}
