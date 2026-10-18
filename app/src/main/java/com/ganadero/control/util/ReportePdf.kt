package com.ganadero.control.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.ganadero.control.data.Animal
import com.ganadero.control.data.Finanza
import com.ganadero.control.data.Produccion
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GENERADOR DE REPORTES PDF (SIN LIBRERÍAS EXTERNAS):
 * Usa android.graphics.pdf.PdfDocument (nativo) para crear un reporte con
 * 3 secciones: Inventario de animales, Producción de leche y Finanzas.
 * Totalmente offline.
 */
object ReportePdf {

    private const val PAGE_WIDTH = 595   // A4 en puntos
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 20f

    // Colores
    private val COLOR_HEADER = Color.parseColor("#E67E22")
    private val COLOR_TEXT = Color.parseColor("#2C2C2C")
    private val COLOR_SECONDARY = Color.parseColor("#7A7A7A")
    private val COLOR_ROW_ALT = Color.parseColor("#F5F0EB")

    fun generar(
        animales: List<Animal>,
        producciones: List<Produccion>,
        finanzas: List<Finanza>
    ): ByteArray {
        val documento = PdfDocument()
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val paintTitulo = Paint().apply {
            color = COLOR_HEADER
            textSize = 20f
            isFakeBoldText = true
        }
        val paintFecha = Paint().apply {
            color = COLOR_SECONDARY
            textSize = 11f
        }
        val paintSeccion = Paint().apply {
            color = COLOR_HEADER
            textSize = 14f
            isFakeBoldText = true
        }
        val paintTexto = Paint().apply {
            color = COLOR_TEXT
            textSize = 10f
        }
        val paintTextoBold = Paint().apply {
            color = COLOR_TEXT
            textSize = 10f
            isFakeBoldText = true
        }
        val paintFilaAlterna = Paint().apply {
            color = COLOR_ROW_ALT
            style = Paint.Style.FILL
        }

        var pageNumber = 1
        var pagina = documento.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = pagina.canvas
        var y = MARGIN + 20f

        // Encabezado del reporte
        canvas.drawText("Reporte Ganadero Control", MARGIN, y, paintTitulo)
        y += 24f
        canvas.drawText("Generado: $fecha", MARGIN, y, paintFecha)
        y += 30f

        // ---- SECCIÓN 1: INVENTARIO DE ANIMALES ----
        canvas.drawText("1. Inventario de Animales (${animales.size})", MARGIN, y, paintSeccion)
        y += 22f

        // Encabezado de tabla
        val colsInv = floatArrayOf(MARGIN, MARGIN + 70, MARGIN + 180, MARGIN + 270, MARGIN + 350, MARGIN + 430)
        val headersInv = arrayOf("Código", "Nombre", "Raza", "Tipo", "Peso", "Estado")
        headersInv.forEachIndexed { i, h ->
            canvas.drawText(h, colsInv[i], y, paintTextoBold)
        }
        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintTextoBold)
        y += 16f

        animales.forEachIndexed { index, a ->
            if (y > PAGE_HEIGHT - MARGIN - 40) {
                documento.finishPage(pagina)
                pageNumber++
                pagina = documento.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = pagina.canvas
                y = MARGIN + 20f
            }
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN - 4, y - 12, PAGE_WIDTH - MARGIN, y + 6, paintFilaAlterna)
            }
            canvas.drawText(a.codigo.ifBlank { "-" }, colsInv[0], y, paintTexto)
            canvas.drawText(a.nombre.ifBlank { "-" }.take(14), colsInv[1], y, paintTexto)
            canvas.drawText(a.raza.ifBlank { "-" }.take(12), colsInv[2], y, paintTexto)
            canvas.drawText(a.tipo, colsInv[3], y, paintTexto)
            canvas.drawText("${a.pesoActual} kg", colsInv[4], y, paintTexto)
            canvas.drawText(if (a.activo) "Activo" else "Inactivo", colsInv[5], y, paintTexto)
            y += LINE_HEIGHT
        }

        y += 20f

        // ---- SECCIÓN 2: PRODUCCIÓN DE LECHE ----
        if (y > PAGE_HEIGHT - MARGIN - 120) {
            documento.finishPage(pagina)
            pageNumber++
            pagina = documento.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = pagina.canvas
            y = MARGIN + 20f
        }
        canvas.drawText("2. Producción de Leche", MARGIN, y, paintSeccion)
        y += 22f

        val totalLitros = producciones.sumOf { it.litros }
        val promedio = if (producciones.isEmpty()) 0.0 else totalLitros / producciones.size
        canvas.drawText("Total de registros: ${producciones.size}", MARGIN, y, paintTexto)
        y += LINE_HEIGHT
        canvas.drawText("Total producido: ${"%.1f".format(totalLitros)} litros", MARGIN, y, paintTexto)
        y += LINE_HEIGHT
        canvas.drawText("Promedio por registro: ${"%.1f".format(promedio)} litros", MARGIN, y, paintTexto)
        y += 24f

        // Últimos registros
        val ultimos = producciones.take(10)
        if (ultimos.isNotEmpty()) {
            canvas.drawText("Últimos registros:", MARGIN, y, paintTextoBold)
            y += 18f
            val colsProd = floatArrayOf(MARGIN, MARGIN + 120, MARGIN + 240, MARGIN + 360)
            arrayOf("Animal", "Fecha", "Turno", "Litros").forEachIndexed { i, h ->
                canvas.drawText(h, colsProd[i], y, paintTextoBold)
            }
            y += 16f
            ultimos.forEach { p ->
                if (y > PAGE_HEIGHT - MARGIN - 40) {
                    documento.finishPage(pagina)
                    pageNumber++
                    pagina = documento.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                    canvas = pagina.canvas
                    y = MARGIN + 20f
                }
                val animal = animales.find { it.id == p.animalId }
                canvas.drawText(animal?.codigo ?: "#${p.animalId}", colsProd[0], y, paintTexto)
                canvas.drawText(p.fecha, colsProd[1], y, paintTexto)
                canvas.drawText(p.turno, colsProd[2], y, paintTexto)
                canvas.drawText("${p.litros} L", colsProd[3], y, paintTexto)
                y += LINE_HEIGHT
            }
        }

        y += 20f

        // ---- SECCIÓN 3: FINANZAS ----
        if (y > PAGE_HEIGHT - MARGIN - 100) {
            documento.finishPage(pagina)
            pageNumber++
            pagina = documento.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = pagina.canvas
            y = MARGIN + 20f
        }
        canvas.drawText("3. Finanzas", MARGIN, y, paintSeccion)
        y += 22f

        val ingresos = finanzas.filter { it.tipo.equals("Ingreso", ignoreCase = true) }.sumOf { it.monto }
        val gastos = finanzas.filter { it.tipo.equals("Gasto", ignoreCase = true) }.sumOf { it.monto }
        val utilidad = ingresos - gastos

        canvas.drawText("Total ingresos: ${"$%,.0f".format(ingresos)}", MARGIN, y, paintTexto)
        y += LINE_HEIGHT
        canvas.drawText("Total gastos: ${"$%,.0f".format(gastos)}", MARGIN, y, paintTexto)
        y += LINE_HEIGHT
        canvas.drawText("Utilidad neta: ${"$%,.0f".format(utilidad)}", MARGIN, y, paintTextoBold)
        y += LINE_HEIGHT
        canvas.drawText("Movimientos registrados: ${finanzas.size}", MARGIN, y, paintTexto)

        documento.finishPage(pagina)

        val salida = ByteArrayOutputStream()
        documento.writeTo(salida)
        documento.close()
        return salida.toByteArray()
    }
}
