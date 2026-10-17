package com.ganadero.control.data

import android.content.Context
import kotlinx.coroutines.runBlocking

object Repo {
    private lateinit var db: GanaderoDatabase

    val animales = mutableListOf<Animal>()
    val producciones = mutableListOf<Produccion>()
    val vacunas = mutableListOf<Vacuna>()
    val finanzas = mutableListOf<Finanza>()

    fun init(ctx: Context) {
        db = GanaderoDatabase.getDatabase(ctx)
        loadFromDb()
    }

    private fun loadFromDb() {
        runBlocking {
            animales.clear()
            animales.addAll(db.animalDao().getAllAnimals().map { it.toDomain() })

            producciones.clear()
            producciones.addAll(db.produccionDao().getAllProducciones().map { it.toDomain() })

            vacunas.clear()
            vacunas.addAll(db.vacunaDao().getAllVacunas().map { it.toDomain() })

            finanzas.clear()
            finanzas.addAll(db.finanzaDao().getAllFinanzas().map { it.toDomain() })

            if (animales.isEmpty() && producciones.isEmpty() && finanzas.isEmpty()) {
                seed()
            }
        }
    }

    // ---------- Animales ----------
    fun addAnimal(a: Animal) {
        runBlocking {
            val id = db.animalDao().insertAnimal(a.toEntity()).toInt()
            a.id = id
            animales.add(a)
        }
    }

    fun deleteAnimal(id: Int) {
        runBlocking {
            db.animalDao().deleteAnimal(id)
            db.produccionDao().deleteByAnimalId(id)
            db.vacunaDao().deleteByAnimalId(id)
            animales.removeAll { it.id == id }
            producciones.removeAll { it.animalId == id }
            vacunas.removeAll { it.animalId == id }
        }
    }

    fun animalName(id: Int): String =
        animales.firstOrNull { it.id == id }?.let {
            if (it.nombre.isNotEmpty()) it.nombre else it.codigo
        } ?: "General"

    // ---------- Produccion ----------
    fun addProduccion(p: Produccion) {
        runBlocking {
            val id = db.produccionDao().insertProduccion(p.toEntity()).toInt()
            p.id = id
            producciones.add(p)
        }
    }

    fun deleteProduccion(id: Int) {
        runBlocking {
            db.produccionDao().deleteProduccion(id)
            producciones.removeAll { it.id == id }
        }
    }

    // ---------- Vacunas ----------
    fun addVacuna(v: Vacuna) {
        runBlocking {
            val id = db.vacunaDao().insertVacuna(v.toEntity()).toInt()
            v.id = id
            vacunas.add(v)
        }
    }

    fun deleteVacuna(id: Int) {
        runBlocking {
            db.vacunaDao().deleteVacuna(id)
            vacunas.removeAll { it.id == id }
        }
    }

    // ---------- Finanzas ----------
    fun addFinanza(f: Finanza) {
        runBlocking {
            val id = db.finanzaDao().insertFinanza(f.toEntity()).toInt()
            f.id = id
            finanzas.add(f)
        }
    }

    fun deleteFinanza(id: Int) {
        runBlocking {
            db.finanzaDao().deleteFinanza(id)
            finanzas.removeAll { it.id == id }
        }
    }

    // ---------- Datos de ejemplo ----------
    private fun seed() {
        runBlocking {
            val luna = Animal(codigo = "A-001", nombre = "Luna", tipo = "Leche", raza = "Holstein", sexo = "Hembra", pesoActual = 480.0)
            val estrella = Animal(codigo = "A-002", nombre = "Estrella", tipo = "Leche", raza = "Jersey", sexo = "Hembra", pesoActual = 450.0)
            val tormenta = Animal(codigo = "A-003", nombre = "Tormenta", tipo = "Carne", raza = "Brahman", sexo = "Macho", pesoActual = 720.0)

            val id1 = db.animalDao().insertAnimal(luna.toEntity()).toInt()
            val id2 = db.animalDao().insertAnimal(estrella.toEntity()).toInt()
            val id3 = db.animalDao().insertAnimal(tormenta.toEntity()).toInt()

            luna.id = id1; estrella.id = id2; tormenta.id = id3
            animales.addAll(listOf(luna, estrella, tormenta))

            val p1 = Produccion(animalId = id1, fecha = todayStr(), turno = "Mañana", litros = 12.5)
            val p2 = Produccion(animalId = id2, fecha = todayStr(), turno = "Tarde", litros = 10.0)
            val pid1 = db.produccionDao().insertProduccion(p1.toEntity()).toInt()
            val pid2 = db.produccionDao().insertProduccion(p2.toEntity()).toInt()
            p1.id = pid1; p2.id = pid2
            producciones.addAll(listOf(p1, p2))

            val v1 = Vacuna(animalId = id1, tipo = "Fiebre aftosa", fechaAplicacion = todayStr(), proximaFecha = todayStr())
            val vid1 = db.vacunaDao().insertVacuna(v1.toEntity()).toInt()
            v1.id = vid1
            vacunas.add(v1)

            val f1 = Finanza(fecha = todayStr(), tipo = "Ingreso", categoria = "Venta de leche", monto = 350000.0, descripcion = "Venta semanal")
            val f2 = Finanza(fecha = todayStr(), tipo = "Gasto", categoria = "Alimento", monto = 120000.0, descripcion = "Concentrado")
            val fid1 = db.finanzaDao().insertFinanza(f1.toEntity()).toInt()
            val fid2 = db.finanzaDao().insertFinanza(f2.toEntity()).toInt()
            f1.id = fid1; f2.id = fid2
            finanzas.addAll(listOf(f1, f2))
        }
    }

    private fun todayStr(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
}
