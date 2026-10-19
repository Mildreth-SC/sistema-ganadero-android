package com.ganadero.control.data.repository

import com.ganadero.control.data.local.AnimalDao
import com.ganadero.control.data.local.toDomain
import com.ganadero.control.data.local.toEntity
import com.ganadero.control.model.Animal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * REPOSITORIO DE ANIMALES:
 * Abstrae el acceso a la tabla `animales`. La UI consume la implementación
 * inyectada por Hilt (no existe un singleton global).
 */
interface AnimalRepository {
    val animales: Flow<List<Animal>>
    fun getAnimal(id: Int): Flow<Animal?>
    suspend fun addAnimal(a: Animal)
    suspend fun updateAnimal(a: Animal)
    suspend fun deleteAnimal(id: Int)
}

/**
 * Implementación sobre Room.
 * - Lecturas: mapea entidades a modelos de dominio dentro del Flow.
 * - Escrituras: se ejecutan fuera del hilo principal con Dispatchers.IO.
 * - deleteAnimal es atómica: la cascada de producciones/vacunas la aplica SQLite
 *   vía @ForeignKey(ON DELETE CASCADE).
 */
class AnimalRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao
) : AnimalRepository {

    override val animales: Flow<List<Animal>> =
        animalDao.getAllAnimales().map { list -> list.map { it.toDomain() } }

    override fun getAnimal(id: Int): Flow<Animal?> =
        animalDao.getById(id).map { it?.toDomain() }

    override suspend fun addAnimal(a: Animal) = withContext(Dispatchers.IO) {
        animalDao.insertAnimal(a.toEntity())
        Unit
    }

    override suspend fun updateAnimal(a: Animal) = withContext(Dispatchers.IO) {
        animalDao.updateAnimal(a.toEntity())
    }

    override suspend fun deleteAnimal(id: Int) = withContext(Dispatchers.IO) {
        animalDao.deleteAnimalById(id)
    }
}
