package com.ganadero.control.ui.finanzas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadero.control.data.Finanza
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
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FinanzasUiState {
    object Loading : FinanzasUiState
    data class Success(
        val finanzas: List<Finanza>,
        val porEliminar: Finanza? = null
    ) : FinanzasUiState
    data class Error(val message: String) : FinanzasUiState
}

sealed interface FinanzasEvent {
    data class ShowToast(val message: String) : FinanzasEvent
    data class ShowSuccess(val message: String) : FinanzasEvent
}

@HiltViewModel
class FinanzasViewModel @Inject constructor(
    private val repository: GanaderoRepository
) : ViewModel() {

    private val porEliminar = MutableStateFlow<Finanza?>(null)

    val uiState: StateFlow<FinanzasUiState> = combine(
        repository.finanzas,
        porEliminar
    ) { finanzas, aEliminar ->
        FinanzasUiState.Success(finanzas, aEliminar) as FinanzasUiState
    }.catch { e ->
        emit(FinanzasUiState.Error(e.message ?: "Error al cargar las finanzas"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanzasUiState.Loading
    )

    private val _uiEvent = MutableSharedFlow<FinanzasEvent>()
    val uiEvent: SharedFlow<FinanzasEvent> = _uiEvent.asSharedFlow()

    fun addFinanza(f: Finanza) {
        viewModelScope.launch {
            try {
                repository.addFinanza(f)
                _uiEvent.emit(FinanzasEvent.ShowSuccess("Movimiento registrado"))
            } catch (e: Exception) {
                _uiEvent.emit(FinanzasEvent.ShowToast("Error al registrar movimiento"))
            }
        }
    }

    fun updateFinanza(f: Finanza) {
        viewModelScope.launch {
            try {
                repository.updateFinanza(f)
                _uiEvent.emit(FinanzasEvent.ShowSuccess("Movimiento actualizado"))
            } catch (e: Exception) {
                _uiEvent.emit(FinanzasEvent.ShowToast("Error al actualizar movimiento"))
            }
        }
    }

    fun pedirEliminar(f: Finanza) { porEliminar.value = f }
    fun cancelarEliminar() { porEliminar.value = null }

    fun confirmarEliminar() {
        val f = porEliminar.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteFinanza(f.id)
                porEliminar.value = null
                _uiEvent.emit(FinanzasEvent.ShowSuccess("Eliminado de forma exitosa"))
            } catch (e: Exception) {
                _uiEvent.emit(FinanzasEvent.ShowToast("Error al eliminar movimiento"))
            }
        }
    }
}
