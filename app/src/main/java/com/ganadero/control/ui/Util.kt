package com.ganadero.control.ui

import java.util.Calendar
import java.util.TimeZone

fun todayStr(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}

fun millisToDateStr(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

fun dateStrToMillis(dateStr: String): Long {
    try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.clear()
            cal.set(year, month, day)
            return cal.timeInMillis
        }
    } catch (_: Exception) {}
    return Calendar.getInstance().timeInMillis
}

fun last7Days(): List<Pair<String, String>> {
    val names = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val out = mutableListOf<Pair<String, String>>()
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, -6)
    repeat(7) {
        val fecha = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        out.add(fecha to names[cal.get(Calendar.DAY_OF_WEEK) - 1])
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return out
}

fun money(v: Double): String {
    val neg = v < 0
    val abs = kotlin.math.abs(v)
    val s = "%,.0f".format(abs).replace(',', '.')
    return if (neg) "-\$$s" else "\$$s"
}

fun oneDecimal(v: Double): String = "%.1f".format(v)
