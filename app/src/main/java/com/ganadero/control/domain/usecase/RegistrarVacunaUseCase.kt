package com.ganadero.control.domain.usecase

import com.ganadero.control.data.repository.VacunaRepository
import com.ganadero.control.model.Vacuna
import javax.inject.Inject

/**
 * CASO DE USO (CAPA DE DOMINIO):
 * Valida el registro de una vacuna antes de persistirla.
 */
class RegistrarVacunaUseCase @Inject constructor(
    private val vacunaRepository: VacunaRepository
) {
    suspend operator fun invoke(vacuna: Vacuna) {
        require(vacuna.animalId > 0) { "Debe seleccionar un animal" }
        require(vacuna.tipo.isNotBlank()) { "El tipo de vacuna es obligatorio" }
        vacunaRepository.addVacuna(vacuna)
    }
}
