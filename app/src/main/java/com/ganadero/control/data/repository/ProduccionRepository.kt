package com.ganadero.control.data.repository

import com.ganadero.control.data.local.ProduccionDao
import com.ganadero.control.data.local.toDomain
import com.ganadero.control.data.local.toEntity
import com.ganadero.control.model.Produccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * REPOSITORIO DE PRODUCCIÓN DE LECHE:
 * Abstrae el acceso a la tabla `producciones`.
 */
interface ProduccionRepository {
    val producciones: Flow<List<Produccion>>
    fun getProduccionesByAnimal(animalId: Int): Flow<List<Produccion>>
    suspend fun addProduccion(p: Produccion)
    suspend fun updateProduccion(p: Produccion)
    suspend fun deleteProduccion(id: Int)
}

class ProduccionRepositoryImpl @Inject constructor(
    private val produccionDao: ProduccionDao
) : ProduccionRepository {

    override val producciones: Flow<List<Produccion>> =
        produccionDao.getAllProducciones().map { list -> list.map { it.toDomain() } }

    override fun getProduccionesByAnimal(animalId: Int): Flow<List<Produccion>> =
        produccionDao.getByAnimalId(animalId).map { list -> list.map { it.toDomain() } }

    override suspend fun addProduccion(p: Produccion) = withContext(Dispatchers.IO) {
        produccionDao.insertProduccion(p.toEntity())
        Unit
    }

    override suspend fun updateProduccion(p: Produccion) = withContext(Dispatchers.IO) {
        produccionDao.updateProduccion(p.toEntity())
    }

    override suspend fun deleteProduccion(id: Int) = withContext(Dispatchers.IO) {
        produccionDao.deleteProduccionById(id)
    }
}
