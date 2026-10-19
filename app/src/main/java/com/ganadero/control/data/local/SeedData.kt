package com.ganadero.control.data.local

import com.ganadero.control.model.Animal
import com.ganadero.control.model.Finanza
import com.ganadero.control.model.Produccion
import com.ganadero.control.model.Vacuna
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DATOS DE EJEMPLO (SEED) — FICTICIOS.
 * Se insertan únicamente la primera vez que se crea la base de datos (onCreate).
 * Rango de registros: 01/10/2026 → 18/10/2026.
 *
 * Hato (32 animales):
 *  - 16 vacas adultas Girolando + 4 vacas adultas Gyr (4 en descarte).
 *  - 2 toros reproductores (1 en descarte).
 *  - 5 terneras en crecimiento (inactivas).
 *  - 5 terneros vendidos (inactivos) con su ingreso por venta.
 * Producción: 8–12 L/vaca/día en doble ordeño (mañana/tarde).
 * Finanzas: leche a $0.65/L, venta de terneros, gastos (vitaminas, desparasitación y alimento).
 * Vacunas: protocolos técnicos con próxima dosis futura (lista "Próximas dosis").
 */
internal object SeedData {

    private const val PRECIO_LITRO = 0.65
    private const val PRECIO_KG_TERNERO = 1.40
    private const val INICIO = "2026-10-01"
    private const val ANCLA = "2026-10-18" // fecha "hoy" para calcular edades del seed
    private const val DIAS = 18 // 01/10 .. 18/10 (inclusive)

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun poblar(
        animalDao: AnimalDao,
        produccionDao: ProduccionDao,
        vacunaDao: VacunaDao,
        finanzaDao: FinanzaDao
    ) {
        // Fechas del rango 01/10/2026 → 18/10/2026
        val fechas = (0 until DIAS).map { i -> desplazar(INICIO, Calendar.DAY_OF_MONTH, i) }

        // Fecha de nacimiento = ANCLA − n meses
        fun haceMeses(meses: Int): String = desplazar(ANCLA, Calendar.MONTH, -meses)

        // ------------------- 1. ANIMALES -------------------
        val idsVacas = mutableListOf<Int>()
        val idsTerneras = mutableListOf<Int>()
        val idsToros = mutableListOf<Int>()

        // 16 vacas Girolando + 4 Gyr (4 en descarte)
        val girolando = listOf(
            "Luna", "Estrella", "Perla", "Aurora", "Estela", "Alba", "Brisa", "Coral",
            "Dalia", "Azul", "Sol", "Rocío", "Maya", "Oliva", "Duna", "Nácar"
        )
        val gyr = listOf("Gauri", "Indu", "Kamala", "Sita")
        val descarte = setOf("Coral", "Dalia", "Sita", "Indu")
        val gestion = setOf("Aurora", "Estela", "Alba", "Gauri")

        fun estadoDe(nombre: String) = when {
            nombre in descarte -> "Descarte"
            nombre in gestion -> "En gestión"
            else -> "Ideal"
        }

        // partos 1–5 correlacionados con la edad (3–7 años) → ~0.7 partos/año
        fun partosDe(i: Int): Int = ((3 + (i % 5) - 1) - (i % 2)).coerceIn(1, 5)

        girolando.forEachIndexed { i, nombre ->
            val edadAnios = 3 + (i % 5)
            idsVacas += animalDao.insertAnimal(
                Animal(
                    codigo = "VAC-%03d".format(i + 1),
                    nombre = nombre, tipo = "Leche", raza = "Girolando", sexo = "Hembra",
                    fechaNacimiento = haceMeses(edadAnios * 12 + (i % 7)),
                    pesoActual = 455.0 + (i % 6) * 12.0,
                    madreNombre = "M-${100 + i}", padreNombre = "Bravo", padreRaza = "Girolando",
                    estadoReproductivo = estadoDe(nombre), partos = partosDe(i)
                ).toEntity()
            ).toInt()
        }
        gyr.forEachIndexed { i, nombre ->
            val edadAnios = 3 + (i % 5)
            idsVacas += animalDao.insertAnimal(
                Animal(
                    codigo = "VAC-%03d".format(girolando.size + i + 1),
                    nombre = nombre, tipo = "Leche", raza = "Gyr", sexo = "Hembra",
                    fechaNacimiento = haceMeses(edadAnios * 12 + (i % 7)),
                    pesoActual = 425.0 + (i % 5) * 11.0,
                    madreNombre = "G-${200 + i}", padreNombre = "Furia", padreRaza = "Gyr",
                    estadoReproductivo = estadoDe(nombre), partos = partosDe(i + 16)
                ).toEntity()
            ).toInt()
        }

        // 5 terneras en crecimiento (inactivas)
        val terneras = listOf(
            Triple("Chispita", "Girolando", Triple(2, 65.0, "GIR")),
            Triple("Nube", "Gyr", Triple(4, 85.0, "GYR")),
            Triple("Perlita", "Girolando", Triple(6, 125.0, "GIR")),
            Triple("Estrellita", "Gyr", Triple(3, 75.0, "GYR")),
            Triple("Miel", "Girolando", Triple(8, 160.0, "GIR"))
        )
        terneras.forEachIndexed { i, (nombre, raza, datos) ->
            val (meses, peso, _) = datos
            idsTerneras += animalDao.insertAnimal(
                Animal(
                    codigo = "CRI-%03d".format(i + 1),
                    nombre = nombre, tipo = "Leche", raza = raza, sexo = "Hembra",
                    fechaNacimiento = haceMeses(meses), pesoActual = peso,
                    madreNombre = "Luna", padreNombre = "Bravo", padreRaza = "Girolando",
                    activo = false, estadoReproductivo = "Ideal",
                    partos = 0, motivoEstado = "En crecimiento"
                ).toEntity()
            ).toInt()
        }

        // 5 terneros vendidos (inactivos) — datos: meses, peso, raza
        val terneros = listOf(
            Triple("Relámpago Jr.", "Girolando", Triple(8, 180.0, 4)),
            Triple("Canelo", "Gyr", Triple(10, 210.0, 6)),
            Triple("Pintado", "Girolando", Triple(7, 165.0, 9)),
            Triple("Trueno", "Gyr", Triple(12, 250.0, 12)),
            Triple("Copito", "Girolando", Triple(9, 195.0, 15))
        )
        val ventas = ArrayList<Triple<String, Double, Int>>() // descripcion, monto, diaVenta
        terneros.forEachIndexed { i, (nombre, raza, datos) ->
            val (meses, peso, diaVenta) = datos
            animalDao.insertAnimal(
                Animal(
                    codigo = "VEN-%03d".format(i + 1),
                    nombre = nombre, tipo = "Carne", raza = raza, sexo = "Macho",
                    fechaNacimiento = haceMeses(meses), pesoActual = peso,
                    madreNombre = "Estrella", padreNombre = "Bravo", padreRaza = "Girolando",
                    activo = false, estadoReproductivo = "Ideal",
                    partos = 0, motivoEstado = "Vendido"
                ).toEntity()
            )
            ventas += Triple("Venta del ternero $nombre (macho)", redondear1(peso * PRECIO_KG_TERNERO), diaVenta)
        }

        // 2 toros reproductores (1 en descarte)
        listOf(
            Triple("Bravo", "Girolando", "Ideal"),
            Triple("Furia", "Gyr", "Descarte")
        ).forEachIndexed { i, (nombre, raza, estado) ->
            idsToros += animalDao.insertAnimal(
                Animal(
                    codigo = "TOR-%03d".format(i + 1),
                    nombre = nombre, tipo = "Carne", raza = raza, sexo = "Macho",
                    fechaNacimiento = if (nombre == "Bravo") "2021-06-12" else "2020-09-03",
                    pesoActual = if (nombre == "Bravo") 835.0 else 760.0,
                    madreNombre = "Selva", padreNombre = "Campeón", padreRaza = raza,
                    estadoReproductivo = estado
                ).toEntity()
            ).toInt()
        }

        // ------------------- 2. PRODUCCIÓN DE LECHE -------------------
        // 20 vacas × 18 días × 2 ordeños = 720 registros (8–12 L/día por vaca)
        val litrosPorDia = DoubleArray(DIAS)
        val producciones = ArrayList<ProduccionEntity>(idsVacas.size * DIAS * 2)

        idsVacas.forEachIndexed { idx, vacaId ->
            val base = 8.0 + (idx % 5) // 8,9,10,11,12 L por día
            for (d in 0 until DIAS) {
                val variacion = (((d * 3 + idx) % 3) - 1) * 0.4 // −0.4 / 0 / +0.4
                val totalDia = (base + variacion).coerceIn(8.0, 12.0)
                val manana = redondear1(totalDia * 0.55)
                val tarde = redondear1(totalDia - manana)
                producciones += Produccion(
                    animalId = vacaId, fecha = fechas[d], turno = "Mañana", litros = manana
                ).toEntity()
                producciones += Produccion(
                    animalId = vacaId, fecha = fechas[d], turno = "Tarde", litros = tarde
                ).toEntity()
                litrosPorDia[d] += manana + tarde
            }
        }
        produccionDao.insertProducciones(producciones)

        // ------------------- 3. VACUNAS (protocolos técnicos) -------------------
        val vacunas = ArrayList<VacunaEntity>()
        idsVacas.forEach { id ->
            vacunas += Vacuna(animalId = id, tipo = "Fiebre aftosa", fechaAplicacion = "2026-10-03", proximaFecha = "2027-10-03").toEntity()
            vacunas += Vacuna(animalId = id, tipo = "Clostridiosis 8-valente", fechaAplicacion = "2026-10-07", proximaFecha = "2027-10-07").toEntity()
            vacunas += Vacuna(animalId = id, tipo = "Respiratoria (IBR/PI3/BVD)", fechaAplicacion = "2026-10-12", proximaFecha = "2027-04-12").toEntity()
            vacunas += Vacuna(animalId = id, tipo = "Leptospirosis", fechaAplicacion = "2026-10-16", proximaFecha = "2027-04-16").toEntity()
        }
        idsToros.forEach { id ->
            vacunas += Vacuna(animalId = id, tipo = "Fiebre aftosa", fechaAplicacion = "2026-10-03", proximaFecha = "2027-10-03").toEntity()
            vacunas += Vacuna(animalId = id, tipo = "Rabia", fechaAplicacion = "2026-10-04", proximaFecha = "2027-10-04").toEntity()
        }
        idsTerneras.forEach { id ->
            vacunas += Vacuna(animalId = id, tipo = "Clostridiosis (primer esquema)", fechaAplicacion = "2026-10-10", proximaFecha = "2026-11-10").toEntity()
        }
        vacunas.forEach { vacunaDao.insertVacuna(it) }

        // ------------------- 4. FINANZAS -------------------
        // Ingresos: venta de leche diaria a $0.65/L
        for (d in 0 until DIAS) {
            val litros = redondear1(litrosPorDia[d])
            finanzaDao.insertFinanza(
                Finanza(
                    fecha = fechas[d], tipo = "Ingreso", categoria = "Venta de leche",
                    monto = redondear1(litros * PRECIO_LITRO),
                    descripcion = "Venta de $litros L del día"
                ).toEntity()
            )
        }
        // Ingresos: venta de terneros (machos)
        ventas.forEach { (descripcion, monto, diaVenta) ->
            finanzaDao.insertFinanza(
                Finanza(
                    fecha = fechas[diaVenta], tipo = "Ingreso",
                    categoria = "Venta de ganado", monto = monto, descripcion = descripcion
                ).toEntity()
            )
        }
        // Gastos: vitaminas
        listOf(0 to 65.0, 5 to 60.0, 10 to 72.0, 15 to 68.0).forEach { (d, monto) ->
            finanzaDao.insertFinanza(
                Finanza(
                    fecha = fechas[d], tipo = "Gasto", categoria = "Vitaminas",
                    monto = monto, descripcion = "Vitaminas A-D-E y B12 (hato)"
                ).toEntity()
            )
        }
        // Gastos: desparasitación
        listOf(2 to 95.0, 16 to 88.0).forEach { (d, monto) ->
            finanzaDao.insertFinanza(
                Finanza(
                    fecha = fechas[d], tipo = "Gasto", categoria = "Desparasitación",
                    monto = monto, descripcion = "Ivermectina + Levamisol (hato)"
                ).toEntity()
            )
        }
        // Gastos: alimento / concentrado
        listOf(4 to 260.0, 9 to 245.0, 14 to 255.0).forEach { (d, monto) ->
            finanzaDao.insertFinanza(
                Finanza(
                    fecha = fechas[d], tipo = "Gasto", categoria = "Alimento",
                    monto = monto, descripcion = "Concentrado 18% y sal mineral"
                ).toEntity()
            )
        }
    }

    private fun redondear1(valor: Double): Double = Math.round(valor * 10.0) / 10.0

    private fun desplazar(desde: String, campo: Int, cantidad: Int): String {
        val c = Calendar.getInstance().apply {
            time = sdf.parse(desde)!!
            add(campo, cantidad)
        }
        return sdf.format(c.time)
    }
}
