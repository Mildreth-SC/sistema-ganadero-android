package com.ganadero.control.domain.usecase

import com.ganadero.control.data.repository.AnimalRepository
import com.ganadero.control.model.Animal
import javax.inject.Inject

/**
 * CASO DE USO (CAPA DE DOMINIO):
 * Valida y actualiza un animal existente.
 */
class ActualizarAnimalUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(animal: Animal) {
        require(animal.id > 0) { "Animal inválido" }
        require(animal.codigo.isNotBlank()) { "El código es obligatorio" }
        require(animal.nombre.isNotBlank()) { "El nombre es obligatorio" }
        animalRepository.updateAnimal(animal)
    }
}
