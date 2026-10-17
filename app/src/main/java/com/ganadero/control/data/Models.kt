package com.ganadero.control.data

data class Animal(
    var id: Int = 0,
    var codigo: String = "",
    var nombre: String = "",
    var tipo: String = "Mixto",
    var raza: String = "",
    var sexo: String = "Hembra",
    var fechaNacimiento: String = "",
    var estado: String = "Activo",
    var pesoActual: Double = 0.0,
    var madreNombre: String = "",
    var madreRaza: String = "",
    var padreNombre: String = "",
    var padreRaza: String = ""
)

data class Produccion(
    var id: Int = 0,
    var animalId: Int = 0,
    var fecha: String = "",
    var turno: String = "Mañana",
    var litros: Double = 0.0
)

data class Vacuna(
    var id: Int = 0,
    var animalId: Int = 0,
    var tipo: String = "",
    var fechaAplicacion: String = "",
    var proximaFecha: String = ""
)

data class Finanza(
    var id: Int = 0,
    var fecha: String = "",
    var tipo: String = "Ingreso",
    var categoria: String = "",
    var monto: Double = 0.0,
    var descripcion: String = ""
)
