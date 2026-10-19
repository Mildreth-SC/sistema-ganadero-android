package com.ganadero.control.domain.usecase

import com.ganadero.control.data.repository.AnimalRepository
import com.ganadero.control.model.Animal
import javax.inject.Inject

/**
 * CASO DE USO (CAPA DE DOMINIO):
 * Valida las reglas de negocio antes de persistir un nuevo animal.
 * Se invoca con la sintaxis `registrarAnimal(animal)` gracias a `operator fun invoke`.
 */
class RegistrarAnimalUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(animal: Animal) {
        require(animal.codigo.isNotBlank()) { "El código es obligatorio" }
        require(animal.nombre.isNotBlank()) { "El nombre es obligatorio" }
        require(animal.pesoActual >= 0) { "El peso no puede ser negativo" }
        animalRepository.addAnimal(animal)
    }
}
