package es.aviferdev.trackfolio.domain.portfolio

import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType

/**
 * Resumen de la posición agregada de un activo.
 */
data class AssetPosition(
    val netQuantity: Double,
    val averageCostOfRemaining: Double,
    val totalInvestedRemaining: Double,
    val realizedPnL: Double,
    val currentValue: Double,
    val unrealizedPnL: Double,
    val unrealizedPnLPercent: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double,
    val hasCurrentPrice: Boolean,
    val dividendIncome: Double = 0.0
)

data class FifoOpenLot(
    val purchaseTransactionId: String,
    val purchaseDate: Long,
    val pricePerUnit: Double,
    val originalQuantity: Double,
    val remainingQuantity: Double,
    val platformId: String
) {
    val remainingCost: Double get() = remainingQuantity * pricePerUnit
}

data class FifoLotConsumption(
    val purchaseTransactionId: String,
    val purchaseDate: Long,
    val purchasePrice: Double,
    val quantityConsumed: Double,
    val pnl: Double
)

data class FifoSaleMatch(
    val saleTransactionId: String,
    val saleDate: Long,
    val salePrice: Double,
    val saleQuantity: Double,
    val platformId: String,
    val consumed: List<FifoLotConsumption>,
    val realizedPnL: Double
)

data class FifoBreakdown(
    val openLots: List<FifoOpenLot>,
    val saleMatches: List<FifoSaleMatch>
) {
    val hasAnyData: Boolean get() = openLots.isNotEmpty() || saleMatches.isNotEmpty()
}

/**
 * Calculadora pura de portfolio basada en FIFO **por plataforma**.
 *
 * Las ventas solo consumen lotes comprados en la misma plataforma.
 * Si el usuario tiene 3 BTC en Coinbase y 2 BTC en Binance, al vender
 * 2 BTC en Coinbase solo se consumen lotes de Coinbase.
 */
object PortfolioCalculator {

    fun calculate(
        transactions: List<AssetTransaction>,
        currentPrice: Double?,
        dividendIncome: Double = 0.0
    ): AssetPosition {
        val ordered = transactions.sortedWith(compareBy({ it.date }, { it.createdAt }))

        // Cola FIFO por plataforma
        val lotsByPlatform = mutableMapOf<String, ArrayDeque<Lot>>()
        var realizedPnL = 0.0

        for (tx in ordered) {
            val lots = lotsByPlatform.getOrPut(tx.platformId) { ArrayDeque() }
            when (tx.type) {
                AssetTransactionType.BUY  -> lots.addLast(Lot(tx.quantity, tx.pricePerUnit))
                AssetTransactionType.SELL -> realizedPnL += consumeFifo(lots, tx)
            }
        }

        val allLots = lotsByPlatform.values.flatten()
        val netQuantity = allLots.sumOf { it.qtyRemaining }
        val totalInvestedRemaining = allLots.sumOf { it.qtyRemaining * it.pricePerUnit }
        val averageCostOfRemaining =
            if (netQuantity > 0.0) totalInvestedRemaining / netQuantity else 0.0

        val hasPrice = currentPrice != null
        val currentValue = if (hasPrice) netQuantity * currentPrice!! else 0.0
        val unrealizedPnL =
            if (hasPrice && netQuantity > 0.0)
                (currentPrice!! - averageCostOfRemaining) * netQuantity
            else 0.0
        val unrealizedPnLPercent =
            if (hasPrice && averageCostOfRemaining > 0.0)
                ((currentPrice!! - averageCostOfRemaining) / averageCostOfRemaining) * 100.0
            else 0.0

        val totalPnL = realizedPnL + unrealizedPnL + dividendIncome
        val grossInvested = ordered
            .filter { it.isBuy }
            .sumOf { it.quantity * it.pricePerUnit }
        val totalPnLPercent =
            if (grossInvested > 0.0) (totalPnL / grossInvested) * 100.0 else 0.0

        return AssetPosition(
            netQuantity            = netQuantity,
            averageCostOfRemaining = averageCostOfRemaining,
            totalInvestedRemaining = totalInvestedRemaining,
            realizedPnL            = realizedPnL,
            currentValue           = currentValue,
            unrealizedPnL          = unrealizedPnL,
            unrealizedPnLPercent   = unrealizedPnLPercent,
            totalPnL               = totalPnL,
            totalPnLPercent        = totalPnLPercent,
            hasCurrentPrice        = hasPrice,
            dividendIncome         = dividendIncome
        )
    }

    /**
     * Cantidad disponible para vender en una plataforma concreta a una fecha.
     * Solo cuenta las compras de esa plataforma menos las ventas de esa plataforma.
     */
    fun availableQuantityAt(
        transactions: List<AssetTransaction>,
        asOfDate: Long,
        platformId: String,
        excludingTransactionId: String? = null
    ): Double {
        val ordered = transactions
            .asSequence()
            .filter { it.id != excludingTransactionId }
            .filter { it.date <= asOfDate }
            .filter { it.platformId == platformId }
            .sortedWith(compareBy({ it.date }, { it.createdAt }))
            .toList()

        var net = 0.0
        for (tx in ordered) {
            net += if (tx.isBuy) tx.quantity else -tx.quantity
        }
        return net.coerceAtLeast(0.0)
    }

    /**
     * Overload legacy sin filtro de plataforma — devuelve la cantidad global
     * disponible sumando todas las plataformas. Se mantiene por compatibilidad
     * con validaciones genéricas.
     */
    fun availableQuantityAt(
        transactions: List<AssetTransaction>,
        asOfDate: Long,
        excludingTransactionId: String? = null
    ): Double {
        val ordered = transactions
            .asSequence()
            .filter { it.id != excludingTransactionId }
            .filter { it.date <= asOfDate }
            .sortedWith(compareBy({ it.date }, { it.createdAt }))
            .toList()

        var net = 0.0
        for (tx in ordered) {
            net += if (tx.isBuy) tx.quantity else -tx.quantity
        }
        return net.coerceAtLeast(0.0)
    }

    /**
     * Desglose FIFO por plataforma: cada lote y venta preserva su platformId.
     */
    fun breakdown(transactions: List<AssetTransaction>): FifoBreakdown {
        val ordered = transactions.sortedWith(compareBy({ it.date }, { it.createdAt }))

        data class TrackedLot(
            val txId: String,
            val date: Long,
            val pricePerUnit: Double,
            val originalQty: Double,
            var remaining: Double,
            val platformId: String
        )

        val lotsByPlatform = mutableMapOf<String, ArrayDeque<TrackedLot>>()
        val sales = mutableListOf<FifoSaleMatch>()

        for (tx in ordered) {
            val lots = lotsByPlatform.getOrPut(tx.platformId) { ArrayDeque() }
            when (tx.type) {
                AssetTransactionType.BUY -> lots.addLast(
                    TrackedLot(
                        txId         = tx.id,
                        date         = tx.date,
                        pricePerUnit = tx.pricePerUnit,
                        originalQty  = tx.quantity,
                        remaining    = tx.quantity,
                        platformId   = tx.platformId
                    )
                )
                AssetTransactionType.SELL -> {
                    var toSell = tx.quantity
                    val consumed = mutableListOf<FifoLotConsumption>()
                    var realized = 0.0
                    while (toSell > 0.0 && lots.isNotEmpty()) {
                        val lot = lots.first()
                        val consumedQty = minOf(toSell, lot.remaining)
                        val pnl = (tx.pricePerUnit - lot.pricePerUnit) * consumedQty
                        consumed.add(
                            FifoLotConsumption(
                                purchaseTransactionId = lot.txId,
                                purchaseDate          = lot.date,
                                purchasePrice         = lot.pricePerUnit,
                                quantityConsumed      = consumedQty,
                                pnl                   = pnl
                            )
                        )
                        realized += pnl
                        toSell   -= consumedQty
                        if (consumedQty >= lot.remaining) lots.removeFirst()
                        else lot.remaining -= consumedQty
                    }
                    sales.add(
                        FifoSaleMatch(
                            saleTransactionId = tx.id,
                            saleDate          = tx.date,
                            salePrice         = tx.pricePerUnit,
                            saleQuantity      = tx.quantity,
                            platformId        = tx.platformId,
                            consumed          = consumed,
                            realizedPnL       = realized
                        )
                    )
                }
            }
        }

        val openLots = lotsByPlatform.values.flatMap { platformLots ->
            platformLots
                .filter { it.remaining > 0.0 }
                .map {
                    FifoOpenLot(
                        purchaseTransactionId = it.txId,
                        purchaseDate          = it.date,
                        pricePerUnit          = it.pricePerUnit,
                        originalQuantity      = it.originalQty,
                        remainingQuantity     = it.remaining,
                        platformId            = it.platformId
                    )
                }
        }

        return FifoBreakdown(
            openLots    = openLots,
            saleMatches = sales.sortedByDescending { it.saleDate }
        )
    }

    // ── Internals ─────────────────────────────────────────────────────────

    private data class Lot(var qtyRemaining: Double, val pricePerUnit: Double)

    private fun consumeFifo(lots: ArrayDeque<Lot>, tx: AssetTransaction): Double {
        var toSell = tx.quantity
        var pnl = 0.0
        while (toSell > 0.0 && lots.isNotEmpty()) {
            val lot = lots.first()
            val consumed = minOf(toSell, lot.qtyRemaining)
            pnl += (tx.pricePerUnit - lot.pricePerUnit) * consumed
            toSell -= consumed
            if (consumed >= lot.qtyRemaining) {
                lots.removeFirst()
            } else {
                lot.qtyRemaining -= consumed
            }
        }
        return pnl
    }
}
