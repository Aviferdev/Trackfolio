package es.aviferdev.n3to.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import es.aviferdev.n3to.domain.model.AssetPosition
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.AppCurrency
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// TODO Refactorizar

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
    private fun fmtAmt(v: Double, currencySymbol: String = AppCurrency.EUR.symbol) =
        "${fmt.format(v)} $currencySymbol"
    private fun fmtPct(v: Double) = "${"%.1f".format(v)}%"
    private fun fmtQty(v: Double) = NumberFormat.getNumberInstance(Locale("es", "ES"))
        .apply { minimumFractionDigits = 0; maximumFractionDigits = 6 }.format(v)

    actual fun generate(
        data: FiscalReportData,
        password: String?,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        val activity = activityRef?.get() ?: run { onResult(false, "La app no está en primer plano"); return }
        try {
            // 1. Generar PDF sin protección con Android API
            val doc = PdfDocument()
            val renderer = Renderer(doc, data)
            renderer.render()
            doc.finishPage(renderer.currentPage)

            val unprotectedFile = File(context.cacheDir, "informe_fiscal_${data.year}_tmp.pdf")
            FileOutputStream(unprotectedFile).use { doc.writeTo(it) }
            doc.close()

            val outFile = File(context.cacheDir, "informe_fiscal_${data.year}.pdf")

            // 2. Si hay contraseña, proteger con PDFBox
            if (!password.isNullOrBlank()) {
                PDFBoxResourceLoader.init(context)
                val pdDoc = PDDocument.load(unprotectedFile)
                val permissions = AccessPermission().apply {
                    setCanPrint(true)
                    setCanExtractContent(false)
                    setCanModify(false)
                }
                val policy = StandardProtectionPolicy(password, password, permissions)
                policy.encryptionKeyLength = 128
                pdDoc.protect(policy)
                pdDoc.save(outFile)
                pdDoc.close()
                unprotectedFile.delete()
            } else {
                // Sin contraseña: renombrar directamente
                unprotectedFile.renameTo(outFile)
            }

            // 3. Compartir
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

    private inner class Renderer(val doc: PdfDocument, val data: FiscalReportData) {

        private fun fmtAmt(v: Double) = this@PdfReportGenerator.fmtAmt(v, data.currencySymbol)

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
            if (data.incomeTaxBreakdown.isNotEmpty()) {
                drawSection("DESGLOSE FISCAL IRPF ${data.year}")
                drawIrpfTable()
            }
            if (data.yearlyIncomes.isNotEmpty()) {
                drawSection("DETALLE DE INGRESOS POR TIPO DE PAGO")
                drawIncomeDetailSection()
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
            canvas.drawText("Cuenta: ${data.accountName}  ·  Moneda: ${data.currencySymbol}", MARGIN, 44f, pSub)
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
                canvas.drawText(fmtAmt(values[i]), x, y + 16f, p12)
            }
            y += 32f
        }

        private fun drawIrpfTable() {
            val breakdown = data.incomeTaxBreakdown
            val totalGross = breakdown.sumOf { it.grossTotal }
            val totalIrpf  = breakdown.sumOf { it.irpfTotal }
            val totalNet   = breakdown.sumOf { it.netTotal }

            checkBreak(50f)
            val colW = COL_W / 3f
            val totLabels = listOf("Bruto total", "IRPF retenido", "Neto total")
            val totValues = listOf(totalGross, totalIrpf, totalNet)
            val totPaints = listOf(pNormal, pRed, pGreen)
            totLabels.forEachIndexed { i, lbl ->
                val x = MARGIN + i * colW
                canvas.drawText(lbl, x, y, pLabel)
                val p12 = Paint(totPaints[i]).apply { textSize = 11f; isFakeBoldText = true }
                canvas.drawText(fmtAmt(totValues[i]), x, y + 14f, p12)
            }
            y += 28f

            val cols   = listOf("Tipo de ingreso", "Bruto", "IRPF retenido", "Neto", "% Ret.")
            val widths = listOf(150f, 90f, 90f, 90f, 55f)
            drawTableHeader(cols, widths)

            for (item in breakdown) {
                checkBreak(14f)
                val cells = listOf(
                    "${item.incomeType.emoji} ${item.incomeType.label}",
                    fmtAmt(item.grossTotal),
                    fmtAmt(item.irpfTotal),
                    fmtAmt(item.netTotal),
                    fmtPct(item.avgIrpfPercent)
                )
                val paints = listOf(pBold, pNormal, pRed, pGreen, pLabel)
                drawTableRow(cells, widths, paints)
            }

            checkBreak(14f)
            y += 4f
            val pNote = Paint(pLabel).apply { textSize = 7f }
            canvas.drawText("Los ingresos sin tipo asignado se agrupan en 'Ingreso exento'.", MARGIN, y, pNote)
            y += 10f
        }

        private fun drawIncomeDetailSection() {
            val byType = data.yearlyIncomes
                .groupBy { it.incomeType ?: IncomeType.EXEMPT_INCOME }
                .toSortedMap(compareBy { it.ordinal })

            val cols   = listOf("Fecha", "Bruto", "IRPF", "SS", "Com.", "Neto")
            val widths = listOf(55f, 75f, 70f, 70f, 60f, 75f)

            for ((incomeType, txs) in byType) {
                checkBreak(40f)
                // Sub-cabecera del tipo de ingreso (emoji + label)
                val typeHead = Paint(pHead).apply { textSize = 10f }
                canvas.drawText("${incomeType.emoji} ${incomeType.label}", MARGIN, y + 9f, typeHead)
                y += 14f

                val byIssuer = txs.groupBy { it.issuerName ?: "(sin emisor)" }
                for ((issuer, issuerTxs) in byIssuer) {
                    checkBreak(30f)
                    // Nombre del emisor (identado, gris, bold)
                    val issuerPaint = Paint(pBold).apply { color = C_GRAY }
                    canvas.drawText(issuer, MARGIN + 10f, y + 9f, issuerPaint)
                    y += 13f

                    drawTableHeader(cols, widths)

                    var issuerGross = 0.0; var issuerIrpf = 0.0
                    var issuerSs = 0.0; var issuerComm = 0.0; var issuerNet = 0.0

                    for (tx in issuerTxs) {
                        checkBreak(14f)
                        val gross = tx.grossAmount ?: tx.amount
                        val irpf  = tx.taxLines.filter { it.role == TaxRole.INCOME_TAX }.sumOf { it.amount }
                        val ss    = tx.taxLines.filter { it.role == TaxRole.SOCIAL_CONTRIBUTION }.sumOf { it.amount }
                        val comm  = tx.commissionAmount ?: 0.0
                        val net   = tx.amount

                        issuerGross += gross; issuerIrpf += irpf
                        issuerSs += ss; issuerComm += comm; issuerNet += net

                        drawTableRow(
                            listOf(formatDate(tx.date), fmtAmt(gross), fmtAmt(irpf), fmtAmt(ss), fmtAmt(comm), fmtAmt(net)),
                            widths,
                            listOf(pNormal, pNormal, pRed, pLabel, pLabel, pGreen)
                        )
                    }

                    // Subtotal por emisor
                    checkBreak(14f)
                    drawTableRow(
                        listOf("Subtotal $issuer", fmtAmt(issuerGross), fmtAmt(issuerIrpf), fmtAmt(issuerSs), fmtAmt(issuerComm), fmtAmt(issuerNet)),
                        widths,
                        listOf(pBold, pBold, pRed, pLabel, pLabel, pGreen)
                    )
                    y += 2f
                }

                // Total por tipo de ingreso
                val typeGross = txs.sumOf { it.grossAmount ?: it.amount }
                val typeIrpf  = txs.sumOf { tx -> tx.taxLines.filter { it.role == TaxRole.INCOME_TAX }.sumOf { it.amount } }
                val typeSs    = txs.sumOf { tx -> tx.taxLines.filter { it.role == TaxRole.SOCIAL_CONTRIBUTION }.sumOf { it.amount } }
                val typeComm  = txs.sumOf { it.commissionAmount ?: 0.0 }
                val typeNet   = txs.sumOf { it.amount }

                checkBreak(14f)
                drawTableRow(
                    listOf("TOTAL", fmtAmt(typeGross), fmtAmt(typeIrpf), fmtAmt(typeSs), fmtAmt(typeComm), fmtAmt(typeNet)),
                    widths,
                    listOf(pBold, pBold, pRed, pLabel, pLabel, pGreen)
                )

                // Separador entre tipos de ingreso
                checkBreak(4f)
                canvas.drawLine(MARGIN, y, MARGIN + widths.sum(), y, pLine)
                y += 6f
            }
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
                    listOf(monthNames.getOrElse(m - 1) { m.toString() }, fmtAmt(income), fmtAmt(expense), fmtAmt(balance)),
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
                drawTableRow(listOf(debt.personName, dir, fmtAmt(debt.amount), formatDate(debt.date)), widths, listOf(pNormal, pNormal, amtPaint, pNormal))
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
                val unrealStr   = pos.unrealizedPnl?.let { fmtAmt(it) } ?: "Sin precio"
                val unrealPaint = when { pos.unrealizedPnl == null -> pNormal; pos.unrealizedPnl >= 0.0 -> pGreen; else -> pRed }
                val realPaint   = if (pos.realizedPnl >= 0) pGreen else pRed
                drawTableRow(listOf(pos.ticker, pos.name.take(16), pos.categoryName ?: "-", fmtQty(pos.netQuantity), fmtAmt(pos.avgCostBasis), fmtAmt(pos.totalCost), unrealStr, fmtAmt(pos.realizedPnl)), widths1, listOf(pBold, pNormal, pNormal, pNormal, pNormal, pNormal, unrealPaint, realPaint))
            }
            y += 10f; checkBreak(20f)
            val labelPaint = Paint(pHead).apply { textSize = 10f; color = C_PRIMARY }
            canvas.drawText("OPERACIONES DEL AÑO ${data.year}", MARGIN, y, labelPaint); y += 14f
            val cols2   = listOf("Fecha", "Tipo", "Cantidad", "Precio", "Importe", "Comisión")
            val widths2 = listOf(55f, 50f, 65f, 70f, 80f, 55f)
            drawTableHeader(cols2, widths2)
            for (pos in data.assetPositions) {
                val yearTxs = pos.yearTransactions
                if (yearTxs.isEmpty()) continue
                checkBreak(28f)
                // Sub-cabecera del activo
                val assetHead = Paint(pBold).apply { color = C_GRAY }
                canvas.drawText("${pos.ticker} — ${pos.name.take(20)}", MARGIN + 4f, y + 9f, assetHead)
                y += 13f
                var sumBought = 0.0; var sumSold = 0.0
                for (tx in yearTxs) {
                    checkBreak(14f)
                    val tipo = when (tx.type) {
                        AssetTransactionType.BUY          -> "Compra"
                        AssetTransactionType.SELL         -> "Venta"
                        AssetTransactionType.TRANSFER_IN  -> "Trasp. In"
                        AssetTransactionType.TRANSFER_OUT -> "Trasp. Out"
                    }
                    val tipoPaint = if (tx.isBuy) pGreen else if (tx.isSell) pRed else pNormal
                    val commStr   = tx.feeNote?.take(12) ?: "-"
                    if (tx.isBuy) sumBought += tx.grossAmount
                    if (tx.isSell) sumSold += tx.grossAmount
                    drawTableRow(
                        listOf(formatDate(tx.date), tipo, fmtQty(tx.quantity), fmtAmt(tx.pricePerUnit), fmtAmt(tx.grossAmount), commStr),
                        widths2,
                        listOf(pNormal, tipoPaint, pNormal, pNormal, pNormal, pLabel)
                    )
                }
                // Subtotal por activo
                checkBreak(14f)
                val subtotalPaint = Paint(pBold).apply { color = C_PRIMARY; textSize = 9f }
                canvas.drawText(
                    "Compras: ${fmtAmt(sumBought)}  |  Ventas: ${fmtAmt(sumSold)}  |  P&L: ${fmtAmt(pos.realizedPnl)}",
                    MARGIN, y + 9f, subtotalPaint
                )
                y += 12f; y += 2f
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
            canvas.drawText("Informe generado por N3to · ${data.accountName} · Ejercicio ${data.year}", MARGIN, footY + 8f, fp)
        }

        private fun formatDate(epochMillis: Long): String {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            return sdf.format(java.util.Date(epochMillis))
        }
    }
}
