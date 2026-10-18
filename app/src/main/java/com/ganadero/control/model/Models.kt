package com.ganadero.control.model

/**
 * MODELOS DE DOMINIO:
 * Modelos inmutables (val) independientes de Room; la UI y los ViewModels trabajan solo con estos.
 * El estado del animal se expresa con un único campo [activo].
 */
data class Animal(
    val id: Int = 0,
    val codigo: String = "",
    val nombre: String = "",
    val tipo: String = "Mixto",
    val raza: String = "",
    val sexo: String = "Hembra",
    val fechaNacimiento: String = "",
    val pesoActual: Double = 0.0,
    val madreNombre: String = "",
    val madreRaza: String = "",
    val padreNombre: String = "",
    val padreRaza: String = "",
    val fotoUri: String? = null,
    val activo: Boolean = true,
    val estadoReproductivo: String = "Ideal"
)

data class Produccion(
    val id: Int = 0,
    val animalId: Int = 0,
    val fecha: String = "",
    val turno: String = "Mañana",
    val litros: Double = 0.0
)

data class Vacuna(
    val id: Int = 0,
    val animalId: Int = 0,
    val tipo: String = "",
    val fechaAplicacion: String = "",
    val proximaFecha: String = ""
)

data class Finanza(
    val id: Int = 0,
    val fecha: String = "",
    val tipo: String = "Ingreso",
    val categoria: String = "",
    val monto: Double = 0.0,
    val descripcion: String = ""
)
