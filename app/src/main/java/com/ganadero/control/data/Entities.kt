package com.ganadero.control.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CAPA DE DATOS Y PERSISTENCIA LOCAL (ROOM / SQLITE):
 * Entidades inmutables (@Entity) con integridad referencial declarada (@ForeignKey + indices).
 */

@Entity(tableName = "animales")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val sexo: String,
    val fechaNacimiento: String,
    val pesoActual: Double,
    val madreNombre: String,
    val madreRaza: String,
    val padreNombre: String,
    val padreRaza: String,
    val fotoUri: String? = null,
    val activo: Boolean = true,
    val estadoReproductivo: String = "Ideal"
)

@Entity(
    tableName = "producciones",
    foreignKeys = [
        ForeignKey(
            entity = AnimalEntity::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["animalId"])]
)
data class ProduccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val fecha: String,
    val turno: String,
    val litros: Double
)

@Entity(
    tableName = "vacunas",
    foreignKeys = [
        ForeignKey(
            entity = AnimalEntity::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["animalId"])]
)
data class VacunaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val tipo: String,
    val fechaAplicacion: String,
    val proximaFecha: String
)

@Entity(tableName = "finanzas")
data class FinanzaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,
    val tipo: String,
    val categoria: String,
    val monto: Double,
    val descripcion: String
)

/**
 * MAPEADORES (MAPPERS):
 * Funciones de extensión para convertir bidireccionalmente entre Entidades de Room y Modelos de Dominio.
 */

fun AnimalEntity.toDomain() = Animal(
    id = id, codigo = codigo, nombre = nombre, tipo = tipo, raza = raza,
    sexo = sexo, fechaNacimiento = fechaNacimiento,
    pesoActual = pesoActual, madreNombre = madreNombre, madreRaza = madreRaza,
    padreNombre = padreNombre, padreRaza = padreRaza, fotoUri = fotoUri,
    activo = activo, estadoReproductivo = estadoReproductivo
)

fun Animal.toEntity() = AnimalEntity(
    id = id, codigo = codigo, nombre = nombre, tipo = tipo, raza = raza,
    sexo = sexo, fechaNacimiento = fechaNacimiento,
    pesoActual = pesoActual, madreNombre = madreNombre, madreRaza = madreRaza,
    padreNombre = padreNombre, padreRaza = padreRaza, fotoUri = fotoUri,
    activo = activo, estadoReproductivo = estadoReproductivo
)

fun ProduccionEntity.toDomain() = Produccion(
    id = id, animalId = animalId, fecha = fecha, turno = turno, litros = litros
)

fun Produccion.toEntity() = ProduccionEntity(
    id = id, animalId = animalId, fecha = fecha, turno = turno, litros = litros
)

fun VacunaEntity.toDomain() = Vacuna(
    id = id, animalId = animalId, tipo = tipo, fechaAplicacion = fechaAplicacion, proximaFecha = proximaFecha
)

fun Vacuna.toEntity() = VacunaEntity(
    id = id, animalId = animalId, tipo = tipo, fechaAplicacion = fechaAplicacion, proximaFecha = proximaFecha
)

fun FinanzaEntity.toDomain() = Finanza(
    id = id, fecha = fecha, tipo = tipo, categoria = categoria, monto = monto, descripcion = descripcion
)

fun Finanza.toEntity() = FinanzaEntity(
    id = id, fecha = fecha, tipo = tipo, categoria = categoria, monto = monto, descripcion = descripcion
)
