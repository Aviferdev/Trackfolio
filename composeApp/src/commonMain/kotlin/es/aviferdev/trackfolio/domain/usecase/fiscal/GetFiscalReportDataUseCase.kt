package es.aviferdev.trackfolio.domain.usecase.fiscal

import es.aviferdev.trackfolio.domain.model.AssetPosition
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.trackfolio.domain.model.FiscalReportData
import es.aviferdev.trackfolio.domain.model.IncomeTaxType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import es.aviferdev.trackfolio.domain.repository.AssetCategoryRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class GetFiscalReportDataUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val assetCategoryRepository: AssetCategoryRepository
) {
    operator fun invoke(accountId: String, year: String): Flow<FiscalReportData> {

        val baseFlow = combine(
            accountRepository.getAccountById(accountId),
            transactionRepository.getAnnualSummaryByAccount(accountId, year),
            transactionRepository.getMonthlyBreakdown(accountId, year),
            debtRepository.getActiveByAccount(accountId),
            transactionRepository.getIncomeByYear(accountId, year)
        ) { account, annual, monthly, debts, incomes ->
            Base(
                accountName     = account?.name ?: "Cuenta",
                currency        = account?.currency ?: "EUR",
                annualSummary   = annual,
                monthlyBreakdown= monthly,
                debts           = debts,
                yearIncomes     = incomes
            )
        }

        return baseFlow.flatMapLatest { base ->
            combine(
                assetRepository.getAssetsByAccount(accountId),
                assetTransactionRepository.getByAccount(accountId),
                assetCategoryRepository.getAllIncludingArchived()
            ) { assets, allTxs, categories ->

                val categoryMap = categories.associateBy { it.id }
                val txsByAsset  = allTxs.groupBy { it.assetId }

                val positions = assets.map { asset ->
                    val txs = txsByAsset[asset.id].orEmpty().sortedBy { it.date }
                    buildPosition(
                        asset.id, asset.ticker, asset.name,
                        categoryMap[asset.assetCategoryId]?.name,
                        asset.currentPrice, txs, year
                    )
                }

                // ── Ganancias/pérdidas patrimoniales del año (ventas de activos) ──
                val totalRealizedGains  = positions.sumOf { maxOf(0.0, it.realizedPnl) }
                val totalRealizedLosses = positions.sumOf { minOf(0.0, it.realizedPnl) }

                // Combinar desglose IRPF de transacciones + ganancias patrimoniales
                val txBreakdown    = buildTaxBreakdown(base.yearIncomes)
                val assetBreakdown = buildAssetGainsBreakdown(positions)
                val fullBreakdown  = mergeTaxBreakdowns(txBreakdown, assetBreakdown)

                // Resumen anual ajustado: incluir ganancias realizadas como ingreso
                val adjustedSummary = base.annualSummary?.let { s ->
                    s.copy(
                        totalIncome  = s.totalIncome + totalRealizedGains,
                        totalExpense = s.totalExpense + kotlin.math.abs(totalRealizedLosses)
                    )
                }

                FiscalReportData(
                    accountName       = base.accountName,
                    currency          = base.currency,
                    year              = year,
                    generatedAt       = Clock.System.now().toEpochMilliseconds(),
                    annualSummary     = adjustedSummary,
                    monthlyBreakdown  = base.monthlyBreakdown,
                    activeDebts       = base.debts,
                    assetPositions    = positions,
                    incomeTaxBreakdown= fullBreakdown
                )
            }
        }
    }

    // ── Desglose fiscal por tipo de rendimiento ────────────────────────────────
    private fun buildTaxBreakdown(incomes: List<Transaction>): List<FiscalIncomeTaxBreakdown> {
        if (incomes.isEmpty()) return emptyList()

        // Todos los ingresos: los que no tienen taxType → SIN_RETENCION
        return incomes
            .groupBy { it.taxType ?: IncomeTaxType.SIN_RETENCION }
            .map { (taxType, txs) ->
                val grossTotal = txs.sumOf { it.grossAmount ?: it.amount }
                val netTotal   = txs.sumOf { it.amount }
                val irpfTotal  = grossTotal - netTotal
                val avgPct     = if (grossTotal > 0.0) (irpfTotal / grossTotal) * 100.0 else 0.0
                FiscalIncomeTaxBreakdown(
                    taxType        = taxType,
                    count          = txs.size,
                    grossTotal     = grossTotal,
                    netTotal       = netTotal,
                    irpfTotal      = irpfTotal,
                    avgIrpfPercent = avgPct
                )
            }
            .sortedBy { it.taxType.ordinal }
    }

    // ── Ganancias/pérdidas patrimoniales desde posiciones de activos ──────────
    private fun buildAssetGainsBreakdown(positions: List<AssetPosition>): List<FiscalIncomeTaxBreakdown> {
        // Solo posiciones con actividad realizada en el año
        val withActivity = positions.filter { it.totalSold > 0.0 }
        if (withActivity.isEmpty()) return emptyList()

        val totalSold     = withActivity.sumOf { it.totalSold }
        val count         = withActivity.size

        // Las ganancias patrimoniales tributan sin retención previa (bruto = neto)
        return listOf(
            FiscalIncomeTaxBreakdown(
                taxType        = IncomeTaxType.GANANCIAS_PATRIMONIALES,
                count          = count,
                grossTotal     = totalSold,
                netTotal       = totalSold,
                irpfTotal      = 0.0,   // sin retención en origen
                avgIrpfPercent = 0.0
            )
        )
    }

    // ── Fusionar desgloses: si dos listas tienen el mismo taxType, sumar ────
    private fun mergeTaxBreakdowns(
        vararg sources: List<FiscalIncomeTaxBreakdown>
    ): List<FiscalIncomeTaxBreakdown> {
        return sources.flatMap { it }
            .groupBy { it.taxType }
            .map { (taxType, items) ->
                FiscalIncomeTaxBreakdown(
                    taxType        = taxType,
                    count          = items.sumOf { it.count },
                    grossTotal     = items.sumOf { it.grossTotal },
                    netTotal       = items.sumOf { it.netTotal },
                    irpfTotal      = items.sumOf { it.irpfTotal },
                    avgIrpfPercent = run {
                        val totalGross = items.sumOf { it.grossTotal }
                        val totalIrpf  = items.sumOf { it.irpfTotal }
                        if (totalGross > 0.0) (totalIrpf / totalGross) * 100.0 else 0.0
                    }
                )
            }
            .sortedBy { it.taxType.ordinal }
    }

    // ── FIFO simplificado ─────────────────────────────────────────────────────
    private fun buildPosition(
        assetId: String,
        ticker: String,
        name: String,
        categoryName: String?,
        currentPrice: Double?,
        sortedTxs: List<AssetTransaction>,
        year: String
    ): AssetPosition {
        val fifoQueue = ArrayDeque<Pair<Double, Double>>()
        var realizedPnl   = 0.0
        var totalBoughtYear = 0.0
        var totalSoldYear   = 0.0
        val yearTxs         = mutableListOf<AssetTransaction>()

        for (tx in sortedTxs) {
            val isThisYear = epochMillisToYear(tx.date) == year
            when (tx.type) {
                AssetTransactionType.BUY -> {
                    fifoQueue.addLast(tx.quantity to tx.pricePerUnit)
                    if (isThisYear) totalBoughtYear += tx.grossAmount
                }
                AssetTransactionType.SELL -> {
                    var remaining = tx.quantity
                    while (remaining > 0.0 && fifoQueue.isNotEmpty()) {
                        val (lotQty, lotPrice) = fifoQueue.first()
                        val consumed = minOf(lotQty, remaining)
                        realizedPnl += consumed * (tx.pricePerUnit - lotPrice)
                        remaining   -= consumed
                        if (consumed >= lotQty) fifoQueue.removeFirst()
                        else fifoQueue[0] = (lotQty - consumed) to lotPrice
                    }
                    if (isThisYear) totalSoldYear += tx.grossAmount
                }
            }
            if (isThisYear) yearTxs.add(tx)
        }

        val netQuantity  = fifoQueue.sumOf { it.first }
        val totalCost    = fifoQueue.sumOf { it.first * it.second }
        val avgCost      = if (netQuantity > 0.0) totalCost / netQuantity else 0.0
        val currentValue = currentPrice?.let { it * netQuantity }
        val unrealizedPnl = currentValue?.let { it - totalCost }

        return AssetPosition(
            ticker        = ticker,
            name          = name,
            categoryName  = categoryName,
            netQuantity   = netQuantity,
            avgCostBasis  = avgCost,
            totalCost     = totalCost,
            currentPrice  = currentPrice,
            currentValue  = currentValue,
            unrealizedPnl = unrealizedPnl,
            realizedPnl   = realizedPnl,
            totalBought   = totalBoughtYear,
            totalSold     = totalSoldYear,
            yearTransactions = yearTxs
        )
    }

    private fun epochMillisToYear(epochMillis: Long): String {
        val daysSinceEpoch = epochMillis / 86_400_000L
        var year = 1970
        var days = daysSinceEpoch
        while (true) {
            val daysInYear = if (isLeap(year)) 366L else 365L
            if (days < daysInYear) break
            days -= daysInYear
            year++
        }
        return year.toString()
    }

    private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0

    private data class Base(
        val accountName:      String,
        val currency:         String,
        val annualSummary:    es.aviferdev.trackfolio.domain.model.AnnualSummary?,
        val monthlyBreakdown: List<es.aviferdev.trackfolio.domain.model.MonthlyTotals>,
        val debts:            List<es.aviferdev.trackfolio.domain.model.Debt>,
        val yearIncomes:      List<Transaction>
    )
}
