package com.ganadero.control.domain.usecase

import com.ganadero.control.data.repository.ProduccionRepository
import com.ganadero.control.model.Produccion
import javax.inject.Inject

/**
 * CASO DE USO (CAPA DE DOMINIO):
 * Valida el registro de un ordeño antes de persistirlo.
 */
class RegistrarProduccionUseCase @Inject constructor(
    private val produccionRepository: ProduccionRepository
) {
    suspend operator fun invoke(produccion: Produccion) {
        require(produccion.animalId > 0) { "Debe seleccionar un animal" }
        require(produccion.litros > 0) { "Los litros deben ser mayores a 0" }
        produccionRepository.addProduccion(produccion)
    }
}
