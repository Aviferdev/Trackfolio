package es.aviferdev.n3to.domain.pdf

import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.drawAtPoint
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIPopoverPresentationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import kotlin.math.abs

//TODO Refactorizar y modular clase completa.

@OptIn(ExperimentalForeignApi::class)
actual class PdfReportGenerator {

    private val PAGE_W = 595.0
    private val PAGE_H = 842.0
    private val MARGIN  = 40.0

    private val colorPrimary   = UIColor(red = 30/255.0,  green = 58/255.0,  blue = 95/255.0,  alpha = 1.0)
    private val colorGreen     = UIColor(red = 46/255.0,  green = 125/255.0, blue = 50/255.0,  alpha = 1.0)
    private val colorRed       = UIColor(red = 198/255.0, green = 40/255.0,  blue = 40/255.0,  alpha = 1.0)
    private val colorGray      = UIColor(red = 120/255.0, green = 120/255.0, blue = 120/255.0, alpha = 1.0)
    private val colorLightGray = UIColor(red = 220/255.0, green = 220/255.0, blue = 220/255.0, alpha = 1.0)
    private val colorBgSection = UIColor(red = 235/255.0, green = 240/255.0, blue = 248/255.0, alpha = 1.0)
    private val colorFooterBg  = UIColor(red = 245/255.0, green = 245/255.0, blue = 245/255.0, alpha = 1.0)

    actual fun generate(data: FiscalReportData, password: String?, onResult: (Boolean, String?) -> Unit) {
        try {
            val pdfData = NSMutableData()

            // Si hay contraseña, usamos diccionario auxiliar con protección
            val auxDict: Map<Any?, Any?>? = if (!password.isNullOrBlank()) {
                mapOf(
                    "kCGPDFContextUserPassword" to password,
                    "kCGPDFContextOwnerPassword" to password
                )
            } else null

            UIGraphicsBeginPDFContextToData(pdfData, CGRectMake(0.0, 0.0, PAGE_W, PAGE_H), auxDict)
            UIGraphicsBeginPDFPage()

            Renderer(data).render()

            UIGraphicsEndPDFContext()

            val fileURL = NSURL.fileURLWithPath("${NSTemporaryDirectory()}informe_fiscal_${data.year}.pdf")
            pdfData.writeToURL(fileURL, atomically = true)

            val rootVC = getRootViewController()
                ?: run { onResult(false, "No se pudo obtener el contexto de la aplicación."); return }

            val activityVC = UIActivityViewController(
                activityItems        = listOf(fileURL),
                applicationActivities = null
            )

            // ── Soporte iPad: anclar popover ──────────────────────────────────
            val popover = activityVC.popoverPresentationController
            if (popover != null) {
                popover.setSourceView(rootVC.view)
                rootVC.view.bounds.useContents {
                    popover.setSourceRect(CGRectMake(size.width / 2.0, size.height / 2.0, 0.0, 0.0))
                }
            }

            activityVC.completionWithItemsHandler = { _, _, _, error ->
                if (error != null) onResult(false, error.localizedDescription) else onResult(true, null)
            }
            rootVC.presentViewController(activityVC, animated = true, completion = null)

        } catch (e: Exception) {
            onResult(false, e.message ?: "Error al generar el PDF")
        }
    }

    // ── Root view controller (iOS 13+) ────────────────────────────────────────
    private fun getRootViewController(): UIViewController? {
        val windowScene = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull() ?: return null

        val keyWindow = windowScene.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?: windowScene.windows.filterIsInstance<UIWindow>().firstOrNull()

        return keyWindow?.rootViewController
    }

    // =========================================================================
    // Renderer
    // =========================================================================
    private inner class Renderer(val data: FiscalReportData) {

        var y = MARGIN

        val fontTitle   = UIFont.boldSystemFontOfSize(18.0)
        val fontSub     = UIFont.systemFontOfSize(10.0)
        val fontSection = UIFont.boldSystemFontOfSize(13.0)
        val fontLabel   = UIFont.systemFontOfSize(9.0)
        val fontNormal  = UIFont.systemFontOfSize(9.0)
        val fontBold    = UIFont.boldSystemFontOfSize(9.0)
        val fontTH      = UIFont.boldSystemFontOfSize(8.0)
        val fontMedium  = UIFont.boldSystemFontOfSize(12.0)
        val fontSmall   = UIFont.systemFontOfSize(7.0)
        val fontNote    = UIFont.systemFontOfSize(7.0)

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
            if (data.activeDebts.isNotEmpty()) { drawSection("DEUDAS ACTIVAS"); drawDebtsTable() }
            if (data.assetPositions.isNotEmpty()) { drawSection("CARTERA DE INVERSIÓN"); drawPortfolioTable() }
            drawFooter()
        }

        fun checkBreak(needed: Double) {
            if (y + needed > PAGE_H - MARGIN) { UIGraphicsBeginPDFPage(); y = MARGIN }
        }

        fun fillRect(x: Double, top: Double, w: Double, h: Double, color: UIColor) {
            val ctx = UIGraphicsGetCurrentContext() ?: return
            CGContextSetFillColorWithColor(ctx, color.CGColor)
            CGContextFillRect(ctx, CGRectMake(x, top, w, h))
        }

        fun strokeLine(x1: Double, y1: Double, x2: Double, y2: Double, color: UIColor, lw: Double = 0.5) {
            val ctx = UIGraphicsGetCurrentContext() ?: return
            CGContextSetStrokeColorWithColor(ctx, color.CGColor)
            CGContextSetLineWidth(ctx, lw)
            CGContextMoveToPoint(ctx, x1, y1)
            CGContextAddLineToPoint(ctx, x2, y2)
            CGContextStrokePath(ctx)
        }

        /**
         * Dibuja texto usando NSAttributedString.drawAtPoint (UIKit).
         *
         * UIGraphicsBeginPDFContextToData crea un contexto con coordenadas UIKit
         * (origen arriba-izquierda, y creciente hacia abajo), por lo que
         * NSAttributedString.drawAtPoint funciona directamente sin transformar
         * coordenadas. Esto evita los problemas de interop con CoreText
         * (CFStringRef, CGAffineTransform CValue, CFAttributedStringRef).
         */
        fun drawText(text: String, x: Double, top: Double, font: UIFont, color: UIColor) {
            val attrs: Map<Any?, Any?> = mapOf(
                NSFontAttributeName            to font,
                NSForegroundColorAttributeName to color
            )
            @Suppress("CAST_NEVER_SUCCEEDS")
            val nsString = text as NSString
            nsString.drawAtPoint(CGPointMake(x, top), withAttributes = attrs)
        }

        // ── Cabecera ──────────────────────────────────────────────────────────
        fun drawPageHeader() {
            fillRect(0.0, 0.0, PAGE_W, 70.0, colorPrimary)
            drawText("INFORME FISCAL ${data.year}",                               MARGIN, 14.0, fontTitle, UIColor.whiteColor)
            drawText("Cuenta: ${data.accountName}  ·  Moneda: ${"€"}",  MARGIN, 38.0, fontSub,   UIColor.whiteColor)
            drawText("Generado: ${formatDate(data.generatedAt)}",                 MARGIN, 52.0, fontSub,   UIColor.whiteColor)
            y = 86.0
        }

        fun drawSection(title: String) {
            checkBreak(24.0); y += 8.0
            fillRect(MARGIN, y, PAGE_W - 2 * MARGIN, 18.0, colorBgSection)
            drawText(title, MARGIN + 4.0, y + 3.0, fontSection, colorPrimary)
            y += 22.0
        }

        // ── Resumen anual ─────────────────────────────────────────────────────
        fun drawAnnualSummary() {
            val s = data.annualSummary
            if (s == null) { drawText("Sin datos para ${data.year}.", MARGIN, y, fontLabel, colorGray); y += 14.0; return }
            checkBreak(60.0)
            val colW = (PAGE_W - 2 * MARGIN) / 3.0
            listOf("Ingresos totales" to s.totalIncome, "Gastos totales" to s.totalExpense, "Balance neto" to s.balance)
                .forEachIndexed { i, (lbl, v) ->
                    val x = MARGIN + i * colW
                    drawText(lbl, x, y, fontLabel, colorGray)
                    val col = when (i) { 0 -> colorGreen; 1 -> colorRed; else -> if (v >= 0) colorGreen else colorRed }
                    drawText(fmtAmt(v), x, y + 16.0, fontMedium, col)
                }
            y += 32.0
        }

        // ── Desglose IRPF ─────────────────────────────────────────────────────
        fun drawIrpfTable() {
            val breakdown  = data.incomeTaxBreakdown
            val totalGross = breakdown.sumOf { it.grossTotal }
            val totalIrpf  = breakdown.sumOf { it.irpfTotal }
            val totalNet   = breakdown.sumOf { it.netTotal }

            checkBreak(50.0)
            val colW = (PAGE_W - 2 * MARGIN) / 3.0
            listOf("Bruto total" to totalGross, "IRPF retenido" to totalIrpf, "Neto total" to totalNet)
                .forEachIndexed { i, (lbl, v) ->
                    val x = MARGIN + i * colW
                    drawText(lbl, x, y, fontLabel, colorGray)
                    val col = when (i) { 1 -> colorRed; 2 -> colorGreen; else -> UIColor.blackColor }
                    drawText(fmtAmt(v), x, y + 14.0, fontMedium, col)
                }
            y += 28.0

            val cols   = listOf("Tipo de rendimiento", "Bruto", "IRPF retenido", "Neto", "% Ret.")
            val widths = listOf(150.0, 90.0, 90.0, 90.0, 55.0)
            drawTableHeader(cols, widths)

            for (item in breakdown) {
                checkBreak(14.0)
                val cells  = listOf(
                    "${item.incomeType.emoji} ${item.incomeType.label}",
                    fmtAmt(item.grossTotal),
                    fmtAmt(item.irpfTotal),
                    fmtAmt(item.netTotal),
                    fmtPct(item.avgIrpfPercent)
                )
                val colors = listOf(UIColor.blackColor, UIColor.blackColor, colorRed, colorGreen, colorGray)
                drawTableRow(cells, widths, colors)
            }
            checkBreak(14.0); y += 4.0
            drawText("Los ingresos sin tipo asignado se agrupan en 'Ingreso exento'.", MARGIN, y, fontNote, colorGray)
            y += 10.0
        }

        // ── Detalle de ingresos por tipo de pago ──────────────────────────────
        fun drawIncomeDetailSection() {
            val byType = data.yearlyIncomes
                .groupBy { it.incomeType ?: IncomeType.EXEMPT_INCOME }
                .toSortedMap(compareBy { it.ordinal })

            val cols   = listOf("Fecha", "Bruto", "IRPF", "SS", "Com.", "Neto")
            val widths = listOf(55.0, 75.0, 70.0, 70.0, 60.0, 75.0)

            for ((incomeType, txs) in byType) {
                checkBreak(40.0)
                // Sub-cabecera del tipo de ingreso
                drawText("${incomeType.emoji} ${incomeType.label}", MARGIN, y, fontSection, colorPrimary)
                y += 14.0

                val byIssuer = txs.groupBy { it.issuerName ?: "(sin emisor)" }
                for ((issuer, issuerTxs) in byIssuer) {
                    checkBreak(30.0)
                    // Nombre del emisor (identado, gris)
                    drawText(issuer, MARGIN + 10.0, y, fontBold, colorGray)
                    y += 13.0

                    drawTableHeader(cols, widths)

                    var issuerGross = 0.0; var issuerIrpf = 0.0
                    var issuerSs = 0.0; var issuerComm = 0.0; var issuerNet = 0.0

                    for (tx in issuerTxs) {
                        checkBreak(14.0)
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
                            listOf(UIColor.blackColor, UIColor.blackColor, colorRed, colorGray, colorGray, colorGreen)
                        )
                    }

                    // Subtotal por emisor
                    checkBreak(14.0)
                    drawTableRow(
                        listOf("Subtotal $issuer", fmtAmt(issuerGross), fmtAmt(issuerIrpf), fmtAmt(issuerSs), fmtAmt(issuerComm), fmtAmt(issuerNet)),
                        widths,
                        listOf(UIColor.blackColor, UIColor.blackColor, colorRed, colorGray, colorGray, colorGreen)
                    )
                    y += 2.0
                }

                // Total por tipo de ingreso
                val typeGross = txs.sumOf { it.grossAmount ?: it.amount }
                val typeIrpf  = txs.sumOf { tx -> tx.taxLines.filter { it.role == TaxRole.INCOME_TAX }.sumOf { it.amount } }
                val typeSs    = txs.sumOf { tx -> tx.taxLines.filter { it.role == TaxRole.SOCIAL_CONTRIBUTION }.sumOf { it.amount } }
                val typeComm  = txs.sumOf { it.commissionAmount ?: 0.0 }
                val typeNet   = txs.sumOf { it.amount }

                checkBreak(14.0)
                drawTableRow(
                    listOf("TOTAL", fmtAmt(typeGross), fmtAmt(typeIrpf), fmtAmt(typeSs), fmtAmt(typeComm), fmtAmt(typeNet)),
                    widths,
                    listOf(UIColor.blackColor, UIColor.blackColor, colorRed, colorGray, colorGray, colorGreen)
                )

                // Separador entre tipos de ingreso
                checkBreak(4.0)
                val totalW = widths.sum()
                strokeLine(MARGIN, y, MARGIN + totalW, y, colorLightGray)
                y += 6.0
            }
        }

        // ── Tabla mensual ─────────────────────────────────────────────────────
        fun drawMonthlyTable() {
            val cols   = listOf("Mes", "Ingresos", "Gastos", "Balance")
            val widths = listOf(60.0, 110.0, 110.0, 110.0)
            drawTableHeader(cols, widths)
            val monthNames = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
            val byMonth = data.monthlyBreakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
            for (m in 1..12) {
                val row = byMonth[m]; val income = row?.totalIncome ?: 0.0; val expense = row?.totalExpense ?: 0.0; val balance = income - expense
                if (income == 0.0 && expense == 0.0) continue
                checkBreak(14.0)
                drawTableRow(
                    listOf(monthNames.getOrElse(m - 1) { m.toString() }, fmtAmt(income), fmtAmt(expense), fmtAmt(balance)),
                    widths,
                    listOf(UIColor.blackColor, colorGreen, colorRed, if (balance >= 0) colorGreen else colorRed)
                )
            }
            y += 4.0
        }

        // ── Tabla de deudas ───────────────────────────────────────────────────
        fun drawDebtsTable() {
            val cols   = listOf("Persona", "Dirección", "Importe", "Fecha")
            val widths = listOf(120.0, 100.0, 100.0, 75.0)
            drawTableHeader(cols, widths)
            for (debt in data.activeDebts) {
                checkBreak(14.0)
                val dir      = if (debt.direction == DebtDirection.I_OWE) "Te debo" else "Me debe"
                val amtColor = if (debt.direction == DebtDirection.I_OWE) colorRed else colorGreen
                drawTableRow(listOf(debt.personName, dir, fmtAmt(debt.amount), formatDate(debt.date)), widths, listOf(UIColor.blackColor, UIColor.blackColor, amtColor, UIColor.blackColor))
            }
            y += 4.0
        }

        // ── Portfolio ─────────────────────────────────────────────────────────
        fun drawPortfolioTable() {
            val cols1   = listOf("Ticker","Nombre","Categoría","Unidades","P.Medio","Valor total","P&L No Real.","P&L Realizado")
            val widths1 = listOf(45.0, 90.0, 65.0, 50.0, 60.0, 65.0, 65.0, 65.0)
            drawTableHeader(cols1, widths1)
            for (pos in data.assetPositions) {
                if (pos.netQuantity == 0.0 && pos.totalBought == 0.0 && pos.totalSold == 0.0) continue
                checkBreak(14.0)
                val unrealStr   = pos.unrealizedPnl?.let { fmtAmt(it) } ?: "Sin precio"
                val unrealColor = when { pos.unrealizedPnl == null -> UIColor.blackColor; pos.unrealizedPnl >= 0.0 -> colorGreen; else -> colorRed }
                drawTableRow(
                    listOf(pos.ticker, pos.name.take(16), pos.categoryName ?: "-", fmtQty(pos.netQuantity), fmtAmt(pos.avgCostBasis), fmtAmt(pos.totalCost), unrealStr, fmtAmt(pos.realizedPnl)),
                    widths1,
                    listOf(UIColor.blackColor, UIColor.blackColor, UIColor.blackColor, UIColor.blackColor, UIColor.blackColor, UIColor.blackColor, unrealColor, if (pos.realizedPnl >= 0) colorGreen else colorRed)
                )
            }
            y += 10.0; checkBreak(20.0)
            drawText("OPERACIONES DEL AÑO ${data.year}", MARGIN, y, fontSection, colorPrimary); y += 14.0
            val cols2   = listOf("Fecha", "Tipo", "Cantidad", "Precio", "Importe", "Comisión")
            val widths2 = listOf(55.0, 50.0, 65.0, 70.0, 80.0, 55.0)
            drawTableHeader(cols2, widths2)
            for (pos in data.assetPositions) {
                val yearTxs = pos.yearTransactions
                if (yearTxs.isEmpty()) continue
                checkBreak(28.0)
                // Sub-cabecera del activo
                drawText("${pos.ticker} — ${pos.name.take(20)}", MARGIN + 4.0, y, fontBold, colorGray)
                y += 13.0
                var sumBought = 0.0; var sumSold = 0.0
                for (tx in yearTxs) {
                    checkBreak(14.0)
                    val tipo = when (tx.type) {
                        AssetTransactionType.BUY          -> "Compra"
                        AssetTransactionType.SELL         -> "Venta"
                        AssetTransactionType.TRANSFER_IN  -> "Trasp. In"
                        AssetTransactionType.TRANSFER_OUT -> "Trasp. Out"
                    }
                    val tipoColor = if (tx.isBuy) colorGreen else if (tx.isSell) colorRed else UIColor.blackColor
                    val commStr   = tx.feeNote?.take(12) ?: "-"
                    if (tx.isBuy) sumBought += tx.grossAmount
                    if (tx.isSell) sumSold += tx.grossAmount
                    drawTableRow(
                        listOf(formatDate(tx.date), tipo, fmtQty(tx.quantity), fmtAmt(tx.pricePerUnit), fmtAmt(tx.grossAmount), commStr),
                        widths2,
                        listOf(UIColor.blackColor, tipoColor, UIColor.blackColor, UIColor.blackColor, UIColor.blackColor, colorGray)
                    )
                }
                // Subtotal por activo
                checkBreak(14.0)
                drawText(
                    "Compras: ${fmtAmt(sumBought)}  |  Ventas: ${fmtAmt(sumSold)}  |  P&L: ${fmtAmt(pos.realizedPnl)}",
                    MARGIN, y, fontBold, colorPrimary
                )
                y += 12.0; y += 2.0
            }
            y += 4.0
        }

        // ── Helpers de tabla ──────────────────────────────────────────────────
        fun drawTableHeader(cols: List<String>, widths: List<Double>) {
            checkBreak(16.0)
            val totalW = widths.sum()
            fillRect(MARGIN, y, totalW, 14.0, colorPrimary)
            var x = MARGIN + 2.0
            cols.forEachIndexed { i, col -> drawText(col, x, y + 2.0, fontTH, UIColor.whiteColor); x += widths[i] }
            y += 14.0
        }

        fun drawTableRow(cells: List<String>, widths: List<Double>, colors: List<UIColor>) {
            var x = MARGIN + 2.0
            cells.forEachIndexed { i, cell ->
                drawText(cell, x, y, if (i == 0) fontBold else fontNormal, colors.getOrElse(i) { UIColor.blackColor })
                x += widths[i]
            }
            y += 12.0
            strokeLine(MARGIN, y, MARGIN + widths.sum(), y, colorLightGray)
        }

        // ── Pie de página ─────────────────────────────────────────────────────
        fun drawFooter() {
            val footY = PAGE_H - 20.0
            fillRect(0.0, footY - 4.0, PAGE_W, 24.0, colorFooterBg)
            drawText("Informe generado por N3to · ${data.accountName} · Ejercicio ${data.year}", MARGIN, footY + 2.0, fontSmall, colorGray)
        }

        // ── Formato — puro Kotlin, sin String.format (JVM-only) ───────────────

        fun fmtAmt(v: Double): String {
            val sign   = if (v < 0) "-" else ""
            val absVal = abs(v)
            val int_   = absVal.toLong()
            val frac   = ((absVal - int_) * 100 + 0.5).toLong().coerceIn(0, 99)
            val intStr = int_.toString().reversed().chunked(3).joinToString(".").reversed()
            val str    = "$sign$intStr,${frac.toString().padStart(2, '0')}"
            return "$str €"
        }

        fun fmtPct(v: Double): String {
            val intPart  = v.toLong()
            val fracPart = ((v - intPart) * 10 + 0.5).toLong().coerceIn(0, 9)
            return "$intPart,$fracPart%"
        }

        fun fmtQty(v: Double): String {
            if (v == 0.0) return "0"
            val sign    = if (v < 0) "-" else ""
            val absVal  = abs(v)
            val intPart = absVal.toLong()
            val fracRaw = ((absVal - intPart) * 1_000_000 + 0.5).toLong()
            val fracStr = fracRaw.toString().padStart(6, '0').trimEnd('0')
            return "$sign$intPart${if (fracStr.isNotEmpty()) ",$fracStr" else ""}"
        }

        fun formatDate(epochMillis: Long): String {
            val date      = NSDate(epochMillis / 1000.0)
            val formatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy"
                locale     = NSLocale(localeIdentifier = "es_ES")
            }
            return formatter.stringFromDate(date)
        }
    }
}
