package es.aviferdev.n3to.domain.usecase.fiscal

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.AssetPosition
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.repository.AssetCategoryRepository
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.DebtRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetFiscalReportDataUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val assetCategoryRepository: AssetCategoryRepository
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(accountId: String, year: String): Flow<FiscalReportData> {

        val baseFlow = combine(
            accountRepository.getAccountById(accountId),
            transactionRepository.getAnnualSummaryByAccount(accountId, year),
            transactionRepository.getMonthlyBreakdown(accountId, year),
            debtRepository.getActiveByAccount(accountId),
            transactionRepository.getIncomeByYear(accountId, year)
        ) { account, annual, monthly, debts, incomes ->
            Base(
                accountName      = account?.name ?: "Cuenta",
                annualSummary    = annual,
                monthlyBreakdown = monthly,
                debts            = debts,
                yearIncomes      = incomes
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

                val totalRealizedGains  = positions.sumOf { maxOf(0.0, it.realizedPnl) }
                val totalRealizedLosses = positions.sumOf { minOf(0.0, it.realizedPnl) }

                val txBreakdown    = buildTaxBreakdown(base.yearIncomes)
                val assetBreakdown = buildAssetGainsBreakdown(positions)
                val fullBreakdown  = mergeTaxBreakdowns(txBreakdown, assetBreakdown)

                val adjustedSummary = base.annualSummary?.let { s ->
                    s.copy(
                        totalIncome  = s.totalIncome + totalRealizedGains,
                        totalExpense = s.totalExpense + kotlin.math.abs(totalRealizedLosses)
                    )
                }

                FiscalReportData(
                    accountName        = base.accountName,
                    year               = year,
                    generatedAt        = nowMillis(),
                    annualSummary      = adjustedSummary,
                    monthlyBreakdown   = base.monthlyBreakdown,
                    activeDebts        = base.debts,
                    assetPositions     = positions,
                    incomeTaxBreakdown = fullBreakdown,
                    hasNetOnlyIncomes  = base.yearIncomes.any { it.isNetOnly },
                    yearlyIncomes      = base.yearIncomes
                )
            }
        }
    }

    private fun buildTaxBreakdown(incomes: List<Transaction>): List<FiscalIncomeTaxBreakdown> {
        if (incomes.isEmpty()) return emptyList()

        return incomes
            .groupBy { it.incomeType ?: IncomeType.EXEMPT_INCOME }
            .map { (incomeType, txs) ->
                val grossTotal = txs.sumOf { it.grossAmount ?: it.amount }
                val netTotal   = txs.sumOf { it.amount }
                val irpfTotal  = txs.sumOf { tx -> tx.taxLines.filter { it.role == es.aviferdev.n3to.domain.model.TaxRole.INCOME_TAX }.sumOf { it.amount } }
                val ssTotal    = txs.sumOf { tx -> tx.taxLines.filter { it.role == es.aviferdev.n3to.domain.model.TaxRole.SOCIAL_CONTRIBUTION }.sumOf { it.amount } }
                val commTotal  = txs.sumOf { it.commissionAmount ?: 0.0 }
                val avgPct     = if (grossTotal > 0.0) (irpfTotal / grossTotal) * 100.0 else 0.0
                FiscalIncomeTaxBreakdown(
                    incomeType          = incomeType,
                    count               = txs.size,
                    grossTotal          = grossTotal,
                    netTotal            = netTotal,
                    irpfTotal           = irpfTotal,
                    socialSecurityTotal = ssTotal,
                    commissionTotal     = commTotal,
                    avgIrpfPercent      = avgPct
                )
            }
            .sortedBy { it.incomeType.ordinal }
    }

    private fun buildAssetGainsBreakdown(positions: List<AssetPosition>): List<FiscalIncomeTaxBreakdown> {
        val withActivity = positions.filter { it.totalSold > 0.0 }
        if (withActivity.isEmpty()) return emptyList()

        val totalSold = withActivity.sumOf { it.totalSold }
        val count     = withActivity.size

        return listOf(
            FiscalIncomeTaxBreakdown(
                incomeType     = IncomeType.DIVIDEND,  // Ganancias patrimoniales → capital mobiliario
                count          = count,
                grossTotal     = totalSold,
                netTotal       = totalSold,
                irpfTotal      = 0.0,
                avgIrpfPercent = 0.0
            )
        )
    }

    private fun mergeTaxBreakdowns(
        vararg sources: List<FiscalIncomeTaxBreakdown>
    ): List<FiscalIncomeTaxBreakdown> {
        return sources.flatMap { it }
            .groupBy { it.incomeType }
            .map { (incomeType, items) ->
                FiscalIncomeTaxBreakdown(
                    incomeType          = incomeType,
                    count               = items.sumOf { it.count },
                    grossTotal          = items.sumOf { it.grossTotal },
                    netTotal            = items.sumOf { it.netTotal },
                    irpfTotal           = items.sumOf { it.irpfTotal },
                    socialSecurityTotal = items.sumOf { it.socialSecurityTotal },
                    commissionTotal     = items.sumOf { it.commissionTotal },
                    avgIrpfPercent      = run {
                        val totalGross = items.sumOf { it.grossTotal }
                        val totalIrpf  = items.sumOf { it.irpfTotal }
                        if (totalGross > 0.0) (totalIrpf / totalGross) * 100.0 else 0.0
                    }
                )
            }
            .sortedBy { it.incomeType.ordinal }
    }

    private fun buildPosition(
        assetId: String, ticker: String, name: String, categoryName: String?,
        currentPrice: Double?, sortedTxs: List<AssetTransaction>, year: String
    ): AssetPosition {
        val fifoQueue = ArrayDeque<Pair<Double, Double>>()
        var realizedPnl    = 0.0
        var totalBoughtYear = 0.0
        var totalSoldYear   = 0.0
        val yearTxs         = mutableListOf<AssetTransaction>()

        for (tx in sortedTxs) {
            val isThisYear = epochMillisToYear(tx.date) == year
            when (tx.type) {
                AssetTransactionType.BUY, AssetTransactionType.TRANSFER_IN -> {
                    fifoQueue.addLast(tx.quantity to tx.pricePerUnit)
                    if (isThisYear && tx.isBuy) totalBoughtYear += tx.grossAmount
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
                AssetTransactionType.TRANSFER_OUT -> {
                    // Consume lotes sin generar P&L (traspaso fiscal neutro)
                    var remaining = tx.quantity
                    while (remaining > 0.0 && fifoQueue.isNotEmpty()) {
                        val (lotQty, lotPrice) = fifoQueue.first()
                        val consumed = minOf(lotQty, remaining)
                        remaining -= consumed
                        if (consumed >= lotQty) fifoQueue.removeFirst()
                        else fifoQueue[0] = (lotQty - consumed) to lotPrice
                    }
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
            ticker           = ticker,
            name             = name,
            categoryName     = categoryName,
            netQuantity      = netQuantity,
            avgCostBasis     = avgCost,
            totalCost        = totalCost,
            currentPrice     = currentPrice,
            currentValue     = currentValue,
            unrealizedPnl    = unrealizedPnl,
            realizedPnl      = realizedPnl,
            totalBought      = totalBoughtYear,
            totalSold        = totalSoldYear,
            yearTransactions = yearTxs
        )
    }

    private fun epochMillisToYear(epochMillis: Long): String {
        val daysSinceEpoch = epochMillis / 86_400_000L
        var year = 1970; var days = daysSinceEpoch
        while (true) {
            val daysInYear = if (isLeap(year)) 366L else 365L
            if (days < daysInYear) break
            days -= daysInYear; year++
        }
        return year.toString()
    }

    private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0

    private data class Base(
        val accountName:      String,
        val annualSummary:    es.aviferdev.n3to.domain.model.AnnualSummary?,
        val monthlyBreakdown: List<es.aviferdev.n3to.domain.model.MonthlyTotals>,
        val debts:            List<es.aviferdev.n3to.domain.model.Debt>,
        val yearIncomes:      List<Transaction>
    )
}
