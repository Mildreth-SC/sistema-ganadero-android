package com.ganadero.control.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAnimalDao : AnimalDao {
    private val store = MutableStateFlow<List<AnimalEntity>>(emptyList())
    private var nextId = 1

    override fun getAllAnimales(): Flow<List<AnimalEntity>> = store

    override fun getById(id: Int): Flow<AnimalEntity?> =
        store.map { list -> list.find { it.id == id } }

    override suspend fun insertAnimal(animal: AnimalEntity): Long {
        val id = if (animal.id == 0) nextId++ else animal.id
        val stored = animal.copy(id = id)
        store.update { list -> list.filterNot { it.id == id } + stored }
        return id.toLong()
    }

    override suspend fun updateAnimal(animal: AnimalEntity) {
        store.update { list -> list.map { if (it.id == animal.id) animal else it } }
    }

    override suspend fun deleteAnimalById(id: Int) {
        store.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun count(): Int = store.value.size
}

class FakeProduccionDao : ProduccionDao {
    private val store = MutableStateFlow<List<ProduccionEntity>>(emptyList())
    private var nextId = 1

    override fun getAllProducciones(): Flow<List<ProduccionEntity>> = store

    override fun getByAnimalId(animalId: Int): Flow<List<ProduccionEntity>> =
        store.map { list -> list.filter { it.animalId == animalId }.sortedByDescending { it.fecha } }

    override suspend fun insertProduccion(produccion: ProduccionEntity): Long {
        val id = if (produccion.id == 0) nextId++ else produccion.id
        store.update { list -> list + produccion.copy(id = id) }
        return id.toLong()
    }

    override suspend fun deleteProduccionById(id: Int) {
        store.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateProduccion(produccion: ProduccionEntity) {
        store.update { list -> list.map { if (it.id == produccion.id) produccion else it } }
    }

    override suspend fun count(): Int = store.value.size
}

class FakeVacunaDao : VacunaDao {
    private val store = MutableStateFlow<List<VacunaEntity>>(emptyList())
    private var nextId = 1

    override fun getAllVacunas(): Flow<List<VacunaEntity>> = store

    override suspend fun insertVacuna(vacuna: VacunaEntity): Long {
        val id = if (vacuna.id == 0) nextId++ else vacuna.id
        store.update { list -> list + vacuna.copy(id = id) }
        return id.toLong()
    }

    override suspend fun deleteVacunaById(id: Int) {
        store.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateVacuna(vacuna: VacunaEntity) {
        store.update { list -> list.map { if (it.id == vacuna.id) vacuna else it } }
    }

    override suspend fun count(): Int = store.value.size
}

class FakeFinanzaDao : FinanzaDao {
    private val store = MutableStateFlow<List<FinanzaEntity>>(emptyList())
    private var nextId = 1

    override fun getAllFinanzas(): Flow<List<FinanzaEntity>> = store

    override suspend fun insertFinanza(finanza: FinanzaEntity): Long {
        val id = if (finanza.id == 0) nextId++ else finanza.id
        store.update { list -> list + finanza.copy(id = id) }
        return id.toLong()
    }

    override suspend fun deleteFinanzaById(id: Int) {
        store.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateFinanza(finanza: FinanzaEntity) {
        store.update { list -> list.map { if (it.id == finanza.id) finanza else it } }
    }

    override suspend fun count(): Int = store.value.size
}

class GanaderoRepositoryTest {

    private fun repository() = GanaderoRepositoryImpl(
        animalDao = FakeAnimalDao(),
        produccionDao = FakeProduccionDao(),
        vacunaDao = FakeVacunaDao(),
        finanzaDao = FakeFinanzaDao()
    )

    @Test
    fun `addAnimal inserta y el flujo emite el modelo de dominio mapeado`() = runTest {
        val repo = repository()

        repo.addAnimal(Animal(codigo = "A-010", nombre = "Mixa", raza = "Holstein"))

        val animales = repo.animales.first()
        assertEquals(1, animales.size)
        assertEquals("A-010", animales[0].codigo)
        assertEquals("Mixa", animales[0].nombre)
        assertTrue(animales[0].activo)
        assertEquals(1, animales[0].id)
    }

    @Test
    fun `getAnimal devuelve el animal por id y null si no existe`() = runTest {
        val repo = repository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))

        val animal = repo.getAnimal(1).first()
        assertEquals("Luna", animal?.nombre)
        assertNull(repo.getAnimal(99).first())
    }

    @Test
    fun `updateAnimal modifica el registro existente`() = runTest {
        val repo = repository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))

        repo.updateAnimal(Animal(id = 1, codigo = "A-001", nombre = "Luna", activo = false))

        val animal = repo.getAnimal(1).first()
        assertEquals("Luna", animal?.nombre)
        assertEquals(false, animal?.activo)
    }

    @Test
    fun `deleteAnimal elimina el animal del flujo`() = runTest {
        val repo = repository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        repo.addAnimal(Animal(codigo = "A-002", nombre = "Estrella"))

        repo.deleteAnimal(1)

        val animales = repo.animales.first()
        assertEquals(1, animales.size)
        assertEquals("A-002", animales[0].codigo)
    }

    @Test
    fun `addProduccion y addFinanza persisten y mapean correctamente`() = runTest {
        val repo = repository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        repo.addProduccion(Produccion(animalId = 1, fecha = "2026-10-17", turno = "Mañana", litros = 12.5))
        repo.addFinanza(Finanza(fecha = "2026-10-17", tipo = "Gasto", categoria = "Alimento", monto = 1200.0))

        val producciones = repo.producciones.first()
        val finanzas = repo.finanzas.first()

        assertEquals(1, producciones.size)
        assertEquals(12.5, producciones[0].litros, 0.001)
        assertEquals("Gasto", finanzas[0].tipo)
    }
}
