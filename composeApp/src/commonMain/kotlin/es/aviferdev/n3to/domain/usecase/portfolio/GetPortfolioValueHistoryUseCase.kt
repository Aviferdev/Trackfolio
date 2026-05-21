package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Calcula el valor histórico mensual del portfolio (inversiones + renta fija).
 *
 * Para cada mes desde la primera transacción:
 *  1. Determina la cantidad mantenida de cada activo (acumulando txs).
 *  2. Busca el último precio registrado hasta ese mes.
 *  3. Suma valor de inversiones + valor estimado de renta fija.
 *
 * Devuelve una lista ordenada cronológicamente de [PortfolioValuePoint].
 */
class GetPortfolioValueHistoryUseCase(
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val priceHistoryRepository: AssetPriceHistoryRepository,
    private val fixedIncomeRepository: FixedIncomeRepository
) {
    /**
     * Trigger de actualización manual.
     * Incrementar este contador fuerza al [combine] a re-emitir,
     * recalculando el histórico con los datos más recientes de la BD.
     * Útil cuando se actualizan precios desde fuera del PortfolioScreen.
     */
    private val refreshTrigger = MutableStateFlow(0)

    fun triggerRefresh() {
        refreshTrigger.value++
    }

    operator fun invoke(
        accountId: String,
        portfolioId: String? = null
    ): Flow<List<PortfolioValuePoint>> {
        val assetsFlow = assetRepository.getAssetsByAccount(accountId)
        val txsFlow = assetTransactionRepository.getByAccount(accountId)
        val fiFlow = fixedIncomeRepository.getByAccount(accountId)
        val priceFlow = priceHistoryRepository.getByAccount(accountId)

        return combine(
            assetsFlow,
            txsFlow,
            fiFlow,
            priceFlow,
            refreshTrigger
        ) { assets, txs, fiPositions, prices, _ ->
            val priceHistories = prices.groupBy { it.assetId }
            // Filtrar por cartera si se especifica
            val filteredAssets =
                if (portfolioId != null) assets.filter { it.portfolioId == portfolioId } else assets
            val filteredFi =
                if (portfolioId != null) fiPositions.filter { it.portfolioId == portfolioId } else fiPositions
            buildPortfolioValueHistory(filteredAssets, txs, filteredFi, priceHistories)
        }
    }

    private fun buildPortfolioValueHistory(
        assets: List<Asset>,
        txs: List<AssetTransaction>,
        fiPositions: List<FixedIncomePosition>,
        priceHistories: Map<String, List<AssetPriceHistory>>
    ): List<PortfolioValuePoint> {
        val nonArchived = assets.filter { !it.archived }
        if (nonArchived.isEmpty() && fiPositions.isEmpty()) return emptyList()

        // Determinar rango de meses: desde la primera tx hasta hoy
        val allDates = txs.map { it.date } + fiPositions.map { it.startDate }
        if (allDates.isEmpty()) return emptyList()

        val tz = TimeZone.currentSystemDefault()
        val firstDate = Instant.fromEpochMilliseconds(allDates.min())
        val firstLocal = firstDate.toLocalDateTime(tz)
        val now = nowMillis()
        val nowLocal = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz)

        // Generar lista de meses (epoch millis del último día de cada mes)
        val months = generateMonthEnds(
            startYear = firstLocal.year,
            startMonth = firstLocal.monthNumber,
            endYear = nowLocal.year,
            endMonth = nowLocal.monthNumber
        )

        if (months.isEmpty()) return emptyList()

        // Agrupar transacciones por activo
        val txsByAsset = txs.groupBy { it.assetId }

        // Pre-indexar precios: assetId -> lista ordenada asc por recordedAt
        val pricesByAsset = priceHistories.mapValues { (_, history) ->
            history.sortedBy { it.recordedAt }
        }

        // Construir puntos
        return months.mapNotNull { monthEndMillis ->
            var totalValue = 0.0

            // ── Inversiones (activos de mercado) ────────────────────────
            for (asset in nonArchived) {
                val assetTxs = txsByAsset[asset.id].orEmpty()
                val qty = quantityHeldAt(assetTxs, monthEndMillis)
                if (qty <= 0.0) continue

                val price = priceAtDate(
                    prices = pricesByAsset[asset.id].orEmpty(),
                    asOfDate = monthEndMillis,
                    currentPrice = asset.currentPrice
                ) ?: continue

                totalValue += qty * price
            }

            // ── Renta fija ─────────────────────────────────────────────
            for (pos in fiPositions) {
                if (pos.archived) continue
                // Solo incluir si estaba activa en este mes
                if (pos.startDate > monthEndMillis) continue
                if (pos.closedAt != null && pos.closedAt <= monthEndMillis) continue

                val accrued = FixedIncomeCalculator.accruedInterest(
                    principal = pos.principal,
                    interestRate = pos.interestRate,
                    startDate = pos.startDate,
                    maturityDate = pos.maturityDate,
                    asOfDate = monthEndMillis.coerceAtMost(pos.maturityDate)
                )
                totalValue += pos.principal + accrued
            }

            if (totalValue > 0.0) {
                PortfolioValuePoint(date = monthEndMillis, value = totalValue)
            } else {
                null
            }
        }
    }

    /**
     * Calcula la cantidad neta mantenida de un activo hasta una fecha dada,
     * procesando las transacciones en orden cronológico (FIFO simplificado).
     */
    private fun quantityHeldAt(
        txs: List<AssetTransaction>,
        asOfDate: Long
    ): Double {
        var qty = 0.0
        for (tx in txs.sortedBy { it.date }) {
            if (tx.date > asOfDate) break
            when (tx.type) {
                AssetTransactionType.BUY -> qty += tx.quantity
                AssetTransactionType.SELL -> qty -= tx.quantity
                AssetTransactionType.TRANSFER_IN -> qty += tx.quantity
                AssetTransactionType.TRANSFER_OUT -> qty -= tx.quantity
            }
        }
        return qty
    }

    /**
     * Busca el último precio registrado para un activo hasta una fecha dada.
     * Si no hay histórico, devuelve el precio actual como fallback.
     */
    private fun priceAtDate(
        prices: List<AssetPriceHistory>,
        asOfDate: Long,
        currentPrice: Double?
    ): Double? {
        // Buscar el último precio registrado ≤ asOfDate
        val match = prices.lastOrNull { it.recordedAt <= asOfDate }
        if (match != null) return match.price

        // Si no hay histórico previo, usar precio actual (asumiendo que no ha cambiado)
        return currentPrice
    }

    /**
     * Genera los epoch millis del último día (23:59:59) de cada mes en el rango dado.
     */
    private fun generateMonthEnds(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): List<Long> {
        val result = mutableListOf<Long>()
        var year = startYear
        var month = startMonth
        val tz = TimeZone.currentSystemDefault()

        while (year < endYear || (year == endYear && month <= endMonth)) {
            // Último día del mes a las 23:59:59
            val lastDay = when (month) {
                2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            val instant = kotlinx.datetime.LocalDateTime(
                year, month, lastDay, 23, 59, 59
            ).toInstant(tz)
            result.add(instant.toEpochMilliseconds())

            month++
            if (month > 12) {
                month = 1
                year++
            }
        }
        return result
    }
}
