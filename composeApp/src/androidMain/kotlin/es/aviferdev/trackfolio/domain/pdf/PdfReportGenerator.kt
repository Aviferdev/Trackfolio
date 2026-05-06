package es.aviferdev.trackfolio.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import es.aviferdev.trackfolio.domain.model.AssetPosition
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.trackfolio.domain.model.FiscalReportData
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

actual class PdfReportGenerator(private val context: Context) {

    private var activityRef: WeakReference<ComponentActivity>? = null

    fun bindActivity(activity: ComponentActivity) { activityRef = WeakReference(activity) }
    fun unbindActivity() { activityRef = null }

    private val PAGE_W = 595
    private val PAGE_H = 842
    private val MARGIN  = 40f
    private val COL_W   = PAGE_W - 2 * MARGIN

    private val C_PRIMARY   = Color.rgb(30, 58, 95)
    private val C_GREEN     = Color.rgb(46, 125, 50)
    private val C_RED       = Color.rgb(198, 40, 40)
    private val C_GRAY      = Color.rgb(120, 120, 120)
    private val C_LGRAY     = Color.rgb(220, 220, 220)
    private val C_BG_HEADER = Color.rgb(30, 58, 95)
    private val C_WHITE     = Color.WHITE
    private val C_BLACK     = Color.BLACK

    private val fmt = NumberFormat.getNumberInstance(Locale("es", "ES")).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }
    private fun fmtAmt(v: Double, currency: String = "") =
        "${fmt.format(v)}${if (currency.isNotEmpty()) " $currency" else ""}"
    private fun fmtPct(v: Double) = "${"%.1f".format(v)}%"
    private fun fmtQty(v: Double) = NumberFormat.getNumberInstance(Locale("es", "ES"))
        .apply { minimumFractionDigits = 0; maximumFractionDigits = 6 }.format(v)

    actual fun generate(data: FiscalReportData, onResult: (success: Boolean, error: String?) -> Unit) {
        val activity = activityRef?.get() ?: run { onResult(false, "La app no está en primer plano"); return }
        try {
            val doc = PdfDocument()
            val renderer = Renderer(doc, data)
            renderer.render()
            doc.finishPage(renderer.currentPage)

            val outFile = File(context.cacheDir, "informe_fiscal_${data.year}.pdf")
            FileOutputStream(outFile).use { doc.writeTo(it) }
            doc.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Informe Fiscal ${data.year} – ${data.accountName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(intent, "Guardar / Compartir informe fiscal"))
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message ?: "Error al generar el PDF")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private inner class Renderer(val doc: PdfDocument, val data: FiscalReportData) {

        var currentPage: PdfDocument.Page = newPage()
        var canvas: Canvas = currentPage.canvas
        var y = MARGIN

        val pTitle  = Paint().apply { color = C_WHITE;   textSize = 18f; isFakeBoldText = true;  isAntiAlias = true }
        val pSub    = Paint().apply { color = C_WHITE;   textSize = 10f; isAntiAlias = true }
        val pHead   = Paint().apply { color = C_PRIMARY; textSize = 13f; isFakeBoldText = true;  isAntiAlias = true }
        val pLabel  = Paint().apply { color = C_GRAY;    textSize = 9f;  isAntiAlias = true }
        val pNormal = Paint().apply { color = C_BLACK;   textSize = 9f;  isAntiAlias = true }
        val pBold   = Paint().apply { color = C_BLACK;   textSize = 9f;  isFakeBoldText = true;  isAntiAlias = true }
        val pGreen  = Paint().apply { color = C_GREEN;   textSize = 9f;  isFakeBoldText = true;  isAntiAlias = true }
        val pRed    = Paint().apply { color = C_RED;     textSize = 9f;  isFakeBoldText = true;  isAntiAlias = true }
        val pLine   = Paint().apply { color = C_LGRAY;   strokeWidth = 0.5f; style = Paint.Style.STROKE; isAntiAlias = true }
        val pRect   = Paint().apply { isAntiAlias = true }

        fun newPage(): PdfDocument.Page {
            val pi = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, doc.pages.size + 1).create()
            return doc.startPage(pi)
        }

        fun checkBreak(needed: Float) {
            if (y + needed > PAGE_H - MARGIN) {
                doc.finishPage(currentPage); currentPage = newPage(); canvas = currentPage.canvas; y = MARGIN
            }
        }

        fun render() {
            drawPageHeader()
            drawSection("RESUMEN ANUAL")
            drawAnnualSummary()
            // ── IRPF — solo si hay ingresos clasificados ──────────────────────
            if (data.incomeTaxBreakdown.isNotEmpty()) {
                drawSection("DESGLOSE FISCAL IRPF ${data.year}")
                drawIrpfTable()
            }
            drawSection("DESGLOSE MENSUAL")
            drawMonthlyTable()
            if (data.activeDebts.isNotEmpty()) {
                drawSection("DEUDAS ACTIVAS")
                drawDebtsTable()
            }
            if (data.assetPositions.isNotEmpty()) {
                drawSection("CARTERA DE INVERSIÓN")
                drawPortfolioTable()
            }
            drawFooter()
        }

        private fun drawPageHeader() {
            pRect.color = C_BG_HEADER
            canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 70f, pRect)
            canvas.drawText("INFORME FISCAL ${data.year}", MARGIN, 28f, pTitle)
            canvas.drawText("Cuenta: ${data.accountName}  ·  Moneda: ${data.currency}", MARGIN, 44f, pSub)
            canvas.drawText("Generado: ${formatDate(data.generatedAt)}", MARGIN, 58f, pSub)
            y = 86f
        }

        private fun drawSection(title: String) {
            checkBreak(24f); y += 8f
            pRect.color = Color.rgb(235, 240, 248)
            canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 18f, pRect)
            canvas.drawText(title, MARGIN + 4f, y + 13f, pHead)
            y += 22f
        }

        private fun drawAnnualSummary() {
            val s = data.annualSummary
            if (s == null) { canvas.drawText("Sin datos para ${data.year}.", MARGIN, y, pLabel); y += 14f; return }
            checkBreak(60f)
            val colW = COL_W / 3f
            val labels = listOf("Ingresos totales", "Gastos totales", "Balance neto")
            val values = listOf(s.totalIncome, s.totalExpense, s.balance)
            val paints = listOf(pGreen, pRed, if (s.balance >= 0) pGreen else pRed)
            labels.forEachIndexed { i, lbl ->
                val x = MARGIN + i * colW
                canvas.drawText(lbl, x, y, pLabel)
                val p12 = Paint(paints[i]).apply { textSize = 12f }
                canvas.drawText(fmtAmt(values[i], data.currency), x, y + 16f, p12)
            }
            y += 32f
        }

        // ── Desglose IRPF ─────────────────────────────────────────────────────
        private fun drawIrpfTable() {
            val breakdown = data.incomeTaxBreakdown
            val totalGross = breakdown.sumOf { it.grossTotal }
            val totalIrpf  = breakdown.sumOf { it.irpfTotal }
            val totalNet   = breakdown.sumOf { it.netTotal }

            // Totales globales (3 columnas)
            checkBreak(50f)
            val colW = COL_W / 3f
            val totLabels = listOf("Bruto total", "IRPF retenido", "Neto total")
            val totValues = listOf(totalGross, totalIrpf, totalNet)
            val totPaints = listOf(pNormal, pRed, pGreen)
            totLabels.forEachIndexed { i, lbl ->
                val x = MARGIN + i * colW
                canvas.drawText(lbl, x, y, pLabel)
                val p12 = Paint(totPaints[i]).apply { textSize = 11f; isFakeBoldText = true }
                canvas.drawText(fmtAmt(totValues[i], data.currency), x, y + 14f, p12)
            }
            y += 28f

            // Tabla por tipo
            val cols   = listOf("Tipo de rendimiento", "Bruto", "IRPF retenido", "Neto", "% Ret.")
            val widths = listOf(150f, 90f, 90f, 90f, 55f)
            drawTableHeader(cols, widths)

            for (item in breakdown) {
                checkBreak(14f)
                val cells = listOf(
                    "${item.taxType.emoji} ${item.taxType.label}",
                    fmtAmt(item.grossTotal, data.currency),
                    fmtAmt(item.irpfTotal,  data.currency),
                    fmtAmt(item.netTotal,   data.currency),
                    fmtPct(item.avgIrpfPercent)
                )
                val paints = listOf(pBold, pNormal, pRed, pGreen, pLabel)
                drawTableRow(cells, widths, paints)
            }

            // Nota
            checkBreak(14f)
            y += 4f
            val pNote = Paint(pLabel).apply { textSize = 7f }
            canvas.drawText("Los ingresos sin tipo fiscal asignado se agrupan en 'Sin retención / Otro'.", MARGIN, y, pNote)
            y += 10f
        }

        private fun drawMonthlyTable() {
            val cols   = listOf("Mes", "Ingresos", "Gastos", "Balance")
            val widths = listOf(60f, 110f, 110f, 110f)
            drawTableHeader(cols, widths)
            val monthNames = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
            val byMonth = data.monthlyBreakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
            for (m in 1..12) {
                val row = byMonth[m]; val income = row?.totalIncome ?: 0.0; val expense = row?.totalExpense ?: 0.0; val balance = income - expense
                if (income == 0.0 && expense == 0.0) continue
                checkBreak(14f)
                drawTableRow(
                    listOf(monthNames.getOrElse(m - 1) { m.toString() }, fmtAmt(income, data.currency), fmtAmt(expense, data.currency), fmtAmt(balance, data.currency)),
                    widths, listOf(pNormal, pGreen, pRed, if (balance >= 0) pGreen else pRed)
                )
            }
            y += 4f
        }

        private fun drawDebtsTable() {
            val cols   = listOf("Persona", "Dirección", "Importe", "Fecha")
            val widths = listOf(120f, 100f, 100f, 75f)
            drawTableHeader(cols, widths)
            for (debt in data.activeDebts) {
                checkBreak(14f)
                val dir = if (debt.direction == DebtDirection.I_OWE) "Te debo" else "Me debe"
                val amtPaint = if (debt.direction == DebtDirection.I_OWE) pRed else pGreen
                drawTableRow(listOf(debt.personName, dir, fmtAmt(debt.amount, data.currency), formatDate(debt.date)), widths, listOf(pNormal, pNormal, amtPaint, pNormal))
            }
            y += 4f
        }

        private fun drawPortfolioTable() {
            val cols1   = listOf("Ticker", "Nombre", "Categoría", "Unidades", "P.Medio", "Valor total", "P&L No Real.", "P&L Realizado")
            val widths1 = listOf(45f, 90f, 65f, 50f, 60f, 65f, 65f, 65f)
            drawTableHeader(cols1, widths1)
            for (pos in data.assetPositions) {
                if (pos.netQuantity == 0.0 && pos.totalBought == 0.0 && pos.totalSold == 0.0) continue
                checkBreak(14f)
                val unrealStr   = pos.unrealizedPnl?.let { fmtAmt(it, data.currency) } ?: "Sin precio"
                val unrealPaint = when { pos.unrealizedPnl == null -> pNormal; pos.unrealizedPnl >= 0.0 -> pGreen; else -> pRed }
                val realPaint   = if (pos.realizedPnl >= 0) pGreen else pRed
                drawTableRow(listOf(pos.ticker, pos.name.take(16), pos.categoryName ?: "-", fmtQty(pos.netQuantity), fmtAmt(pos.avgCostBasis, data.currency), fmtAmt(pos.totalCost, data.currency), unrealStr, fmtAmt(pos.realizedPnl, data.currency)), widths1, listOf(pBold, pNormal, pNormal, pNormal, pNormal, pNormal, unrealPaint, realPaint))
            }
            y += 10f; checkBreak(20f)
            val labelPaint = Paint(pHead).apply { textSize = 10f; color = C_PRIMARY }
            canvas.drawText("Actividad del año ${data.year}", MARGIN, y, labelPaint); y += 14f
            val cols2   = listOf("Ticker", "Nombre", "Total comprado", "Total vendido", "P&L Realizado año")
            val widths2 = listOf(45f, 100f, 110f, 110f, 130f)
            drawTableHeader(cols2, widths2)
            for (pos in data.assetPositions) {
                if (pos.totalBought == 0.0 && pos.totalSold == 0.0) continue
                checkBreak(14f)
                val pnlPaint = if (pos.realizedPnl >= 0) pGreen else pRed
                drawTableRow(listOf(pos.ticker, pos.name.take(20), fmtAmt(pos.totalBought, data.currency), fmtAmt(pos.totalSold, data.currency), fmtAmt(pos.realizedPnl, data.currency)), widths2, listOf(pBold, pNormal, pGreen, pRed, pnlPaint))
            }
            y += 4f
        }

        private fun drawTableHeader(cols: List<String>, widths: List<Float>) {
            checkBreak(16f)
            pRect.color = Color.rgb(30, 58, 95)
            val totalW = widths.sum()
            canvas.drawRect(MARGIN, y, MARGIN + totalW, y + 14f, pRect)
            val hPaint = Paint(pBold).apply { color = Color.WHITE; textSize = 8f }
            var x = MARGIN + 2f
            cols.forEachIndexed { i, col -> canvas.drawText(col, x, y + 10f, hPaint); x += widths[i] }
            y += 14f
        }

        private fun drawTableRow(cells: List<String>, widths: List<Float>, paints: List<Paint>) {
            var x = MARGIN + 2f
            cells.forEachIndexed { i, cell -> canvas.drawText(cell, x, y + 9f, paints.getOrElse(i) { pNormal }); x += widths[i] }
            y += 12f
            canvas.drawLine(MARGIN, y, MARGIN + widths.sum(), y, pLine)
        }

        private fun drawFooter() {
            val footY = (PAGE_H - 16).toFloat()
            pRect.color = Color.rgb(245, 245, 245)
            canvas.drawRect(0f, footY - 4f, PAGE_W.toFloat(), PAGE_H.toFloat(), pRect)
            val fp = Paint(pLabel).apply { textSize = 7f; color = C_GRAY }
            canvas.drawText("Informe generado por Trackfolio · ${data.accountName} · Ejercicio ${data.year}", MARGIN, footY + 8f, fp)
        }

        private fun formatDate(epochMillis: Long): String {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            return sdf.format(java.util.Date(epochMillis))
        }
    }
}
