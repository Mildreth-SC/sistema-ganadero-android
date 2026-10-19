package com.ganadero.control.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Cálculos de fecha para el detalle de un animal:
 *  - Edad en años y meses a partir de la fecha de nacimiento.
 *  - Partos por año (productividad reproductiva).
 */
object Fechas {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Devuelve la edad como texto: "2 meses", "3 años 4 meses", etc. ("—" si no hay fecha válida). */
    fun calcularEdad(fechaNacimiento: String): String {
        val nacimiento = parsear(fechaNacimiento) ?: return "—"
        val ahora = Calendar.getInstance()
        if (nacimiento.after(ahora)) return "—"

        var anios = ahora.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        var meses = ahora.get(Calendar.MONTH) - nacimiento.get(Calendar.MONTH)
        if (ahora.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH)) meses--
        if (meses < 0) {
            anios--
            meses += 12
        }
        return when {
            anios > 0 && meses > 0 -> "$anios años $meses meses"
            anios > 0 -> "$anios años"
            else -> "$meses meses"
        }
    }

    /** Promedio de partos por año de vida del animal ("No aplica" si nunca ha parido). */
    fun partosPorAnio(partos: Int, fechaNacimiento: String): String {
        if (partos <= 0) return "No aplica"
        val nacimiento = parsear(fechaNacimiento) ?: return "—"
        val anios = edadEnMeses(nacimiento) / 12.0
        if (anios <= 0.0) return "—"
        return String.format(Locale.US, "%.2f", partos / anios)
    }

    private fun edadEnMeses(nacimiento: Calendar): Int {
        val ahora = Calendar.getInstance()
        var meses = (ahora.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)) * 12 +
            (ahora.get(Calendar.MONTH) - nacimiento.get(Calendar.MONTH))
        if (ahora.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH)) meses--
        return meses.coerceAtLeast(0)
    }

    private fun parsear(fecha: String): Calendar? {
        if (fecha.isBlank()) return null
        return try {
            Calendar.getInstance().apply { time = sdf.parse(fecha)!! }
        } catch (_: Exception) {
            null
        }
    }
}
