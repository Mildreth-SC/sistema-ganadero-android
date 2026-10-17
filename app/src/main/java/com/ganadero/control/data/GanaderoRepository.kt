package com.ganadero.control.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * PATRÓN REPOSITORIO:
 * Interfaz que abstrae el acceso a la base de datos del resto de la aplicación.
 * La UI consume el repositorio inyectado por Hilt (no existe un singleton global).
 */
interface GanaderoRepository {
    val animales: Flow<List<Animal>>
    val producciones: Flow<List<Produccion>>
    val vacunas: Flow<List<Vacuna>>
    val finanzas: Flow<List<Finanza>>

    fun getAnimal(id: Int): Flow<Animal?>

    suspend fun addAnimal(a: Animal)
    suspend fun updateAnimal(a: Animal)
    suspend fun deleteAnimal(id: Int)

    suspend fun addProduccion(p: Produccion)
    suspend fun updateProduccion(p: Produccion)
    suspend fun deleteProduccion(id: Int)

    suspend fun addVacuna(v: Vacuna)
    suspend fun updateVacuna(v: Vacuna)
    suspend fun deleteVacuna(id: Int)

    suspend fun addFinanza(f: Finanza)
    suspend fun updateFinanza(f: Finanza)
    suspend fun deleteFinanza(id: Int)
}

/**
 * IMPLEMENTACIÓN DEL REPOSITORIO:
 * - Lecturas: mapea entidades Room a modelos de dominio dentro del Flow.
 * - Escrituras: se ejecutan fuera del hilo principal con Dispatchers.IO.
 * - deleteAnimal es una única operación atómica: la cascada de producciones/vacunas
 *   la aplica SQLite vía @ForeignKey(ON DELETE CASCADE).
 */
class GanaderoRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao,
    private val produccionDao: ProduccionDao,
    private val vacunaDao: VacunaDao,
    private val finanzaDao: FinanzaDao
) : GanaderoRepository {

    override val animales: Flow<List<Animal>> =
        animalDao.getAllAnimales().map { list -> list.map { it.toDomain() } }

    override val producciones: Flow<List<Produccion>> =
        produccionDao.getAllProducciones().map { list -> list.map { it.toDomain() } }

    override val vacunas: Flow<List<Vacuna>> =
        vacunaDao.getAllVacunas().map { list -> list.map { it.toDomain() } }

    override val finanzas: Flow<List<Finanza>> =
        finanzaDao.getAllFinanzas().map { list -> list.map { it.toDomain() } }

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

    override suspend fun addVacuna(v: Vacuna) = withContext(Dispatchers.IO) {
        vacunaDao.insertVacuna(v.toEntity())
        Unit
    }

    override suspend fun updateVacuna(v: Vacuna) = withContext(Dispatchers.IO) {
        vacunaDao.updateVacuna(v.toEntity())
    }

    override suspend fun deleteVacuna(id: Int) = withContext(Dispatchers.IO) {
        vacunaDao.deleteVacunaById(id)
    }

    override suspend fun addFinanza(f: Finanza) = withContext(Dispatchers.IO) {
        finanzaDao.insertFinanza(f.toEntity())
        Unit
    }

    override suspend fun updateFinanza(f: Finanza) = withContext(Dispatchers.IO) {
        finanzaDao.updateFinanza(f.toEntity())
    }

    override suspend fun deleteFinanza(id: Int) = withContext(Dispatchers.IO) {
        finanzaDao.deleteFinanzaById(id)
    }
}
