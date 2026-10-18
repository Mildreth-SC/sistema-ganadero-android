package com.ganadero.control.ui.animales

import com.ganadero.control.data.Animal
import com.ganadero.control.data.Finanza
import com.ganadero.control.data.GanaderoRepository
import com.ganadero.control.data.Produccion
import com.ganadero.control.data.Vacuna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeGanaderoRepository : GanaderoRepository {
    private val _animales = MutableStateFlow<List<Animal>>(emptyList())
    private var nextId = 1

    val animalesState: StateFlow<List<Animal>> = _animales

    override val animales: Flow<List<Animal>> = _animales
    override val producciones: Flow<List<Produccion>> = MutableStateFlow(emptyList())
    override val vacunas: Flow<List<Vacuna>> = MutableStateFlow(emptyList())
    override val finanzas: Flow<List<Finanza>> = MutableStateFlow(emptyList())

    override fun getAnimal(id: Int): Flow<Animal?> = _animales.map { list -> list.find { it.id == id } }

    override fun getProduccionesByAnimal(animalId: Int): Flow<List<Produccion>> = MutableStateFlow(emptyList())

    override suspend fun addAnimal(a: Animal) {
        val id = if (a.id == 0) nextId++ else a.id
        _animales.update { list -> list.filterNot { it.id == id } + a.copy(id = id) }
    }

    override suspend fun updateAnimal(a: Animal) {
        _animales.update { list -> list.map { if (it.id == a.id) a else it } }
    }

    override suspend fun deleteAnimal(id: Int) {
        _animales.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun addProduccion(p: Produccion) = Unit
    override suspend fun updateProduccion(p: Produccion) = Unit
    override suspend fun deleteProduccion(id: Int) = Unit
    override suspend fun addVacuna(v: Vacuna) = Unit
    override suspend fun updateVacuna(v: Vacuna) = Unit
    override suspend fun deleteVacuna(id: Int) = Unit
    override suspend fun addFinanza(f: Finanza) = Unit
    override suspend fun updateFinanza(f: Finanza) = Unit
    override suspend fun deleteFinanza(id: Int) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun TestScope.collectUiState(viewModel: AnimalesViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }

    @Test
    fun `uiState inicia en Loading y pasa a Success con los datos del repositorio`() = runTest {
        val repo = FakeGanaderoRepository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        val viewModel = AnimalesViewModel(repo)

        assertEquals(AnimalesUiState.Loading, viewModel.uiState.value)

        collectUiState(viewModel)
        val state = viewModel.uiState.value
        assertTrue(state is AnimalesUiState.Success)
        assertEquals("Luna", (state as AnimalesUiState.Success).animales[0].nombre)
    }

    @Test
    fun `addAnimal emite evento de exito y actualiza el estado`() = runTest {
        val repo = FakeGanaderoRepository()
        val viewModel = AnimalesViewModel(repo)
        collectUiState(viewModel)

        val events = mutableListOf<AnimalesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))

        assertEquals(1, events.size)
        assertTrue(events[0] is AnimalesEvent.ShowSuccess)
        val state = viewModel.uiState.value as AnimalesUiState.Success
        assertEquals(listOf("Luna"), state.animales.map { it.nombre })
    }

    @Test
    fun `updateAnimal emite ShowSuccess (semantica unificada de eventos)`() = runTest {
        val repo = FakeGanaderoRepository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        val viewModel = AnimalesViewModel(repo)
        collectUiState(viewModel)

        val events = mutableListOf<AnimalesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.updateAnimal(Animal(id = 1, codigo = "A-001", nombre = "Luna", activo = false))

        assertEquals(1, events.size)
        assertTrue(events[0] is AnimalesEvent.ShowSuccess)
        assertEquals(false, (viewModel.uiState.value as AnimalesUiState.Success).animales[0].activo)
    }

    @Test
    fun `getAnimal devuelve solo el animal solicitado`() = runTest {
        val repo = FakeGanaderoRepository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        repo.addAnimal(Animal(codigo = "A-002", nombre = "Estrella"))
        val viewModel = AnimalesViewModel(repo)

        val animal = viewModel.getAnimal(2).first()
        assertEquals("Estrella", animal?.nombre)
    }

    @Test
    fun `eliminarAnimal elimina y emite ShowSuccess`() = runTest {
        val repo = FakeGanaderoRepository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        val viewModel = AnimalesViewModel(repo)
        collectUiState(viewModel)

        val events = mutableListOf<AnimalesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.eliminarAnimal(Animal(id = 1, codigo = "A-001", nombre = "Luna"))

        assertEquals(1, events.size)
        assertTrue(events[0] is AnimalesEvent.ShowSuccess)
        assertTrue((viewModel.uiState.value as AnimalesUiState.Success).animales.isEmpty())
    }

    @Test
    fun `pedir y confirmar eliminar usa el estado porEliminar`() = runTest {
        val repo = FakeGanaderoRepository()
        repo.addAnimal(Animal(codigo = "A-001", nombre = "Luna"))
        val viewModel = AnimalesViewModel(repo)
        collectUiState(viewModel)

        viewModel.pedirEliminar(Animal(id = 1, codigo = "A-001", nombre = "Luna"))
        assertEquals("Luna", (viewModel.uiState.value as AnimalesUiState.Success).animalPorEliminar?.nombre)

        viewModel.cancelarEliminar()
        assertEquals(null, (viewModel.uiState.value as AnimalesUiState.Success).animalPorEliminar)

        viewModel.pedirEliminar(Animal(id = 1, codigo = "A-001", nombre = "Luna"))
        viewModel.confirmarEliminar()
        assertTrue((viewModel.uiState.value as AnimalesUiState.Success).animales.isEmpty())
    }
}
