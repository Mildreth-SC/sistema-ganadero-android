package com.ganadero.control.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val sexo: String,
    val fechaNacimiento: String,
    val estado: String,
    val pesoActual: Double,
    val madreNombre: String,
    val madreRaza: String,
    val padreNombre: String,
    val padreRaza: String
)

@Entity(tableName = "producciones")
data class ProduccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val fecha: String,
    val turno: String,
    val litros: Double
)

@Entity(tableName = "vacunas")
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

// Mapeadores
fun AnimalEntity.toDomain() = Animal(
    id = id, codigo = codigo, nombre = nombre, tipo = tipo, raza = raza,
    sexo = sexo, fechaNacimiento = fechaNacimiento, estado = estado,
    pesoActual = pesoActual, madreNombre = madreNombre, madreRaza = madreRaza,
    padreNombre = padreNombre, padreRaza = padreRaza
)

fun Animal.toEntity() = AnimalEntity(
    id = id, codigo = codigo, nombre = nombre, tipo = tipo, raza = raza,
    sexo = sexo, fechaNacimiento = fechaNacimiento, estado = estado,
    pesoActual = pesoActual, madreNombre = madreNombre, madreRaza = madreRaza,
    padreNombre = padreNombre, padreRaza = padreRaza
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
