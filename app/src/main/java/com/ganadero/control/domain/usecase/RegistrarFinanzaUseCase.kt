package com.ganadero.control.domain.usecase

import com.ganadero.control.data.repository.FinanzaRepository
import com.ganadero.control.model.Finanza
import javax.inject.Inject

/**
 * CASO DE USO (CAPA DE DOMINIO):
 * Valida el registro de un movimiento financiero antes de persistirlo.
 */
class RegistrarFinanzaUseCase @Inject constructor(
    private val finanzaRepository: FinanzaRepository
) {
    suspend operator fun invoke(finanza: Finanza) {
        require(finanza.tipo.isNotBlank()) { "El tipo de movimiento es obligatorio" }
        require(finanza.monto > 0) { "El monto debe ser mayor a 0" }
        finanzaRepository.addFinanza(finanza)
    }
}
