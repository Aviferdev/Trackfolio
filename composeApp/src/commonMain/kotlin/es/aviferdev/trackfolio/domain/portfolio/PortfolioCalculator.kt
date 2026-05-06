package es.aviferdev.trackfolio.domain.portfolio

import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType

/**
 * Resumen de la posición agregada de un activo a partir de sus movimientos
 * (FIFO) y de su precio actual centralizado.
 *
 * - `netQuantity`: unidades en cartera tras aplicar todas las compras y ventas.
 * - `averageCostOfRemaining`: precio medio ponderado de las unidades aún en
 *   cartera (no incluye lotes ya consumidos por ventas).
 * - `totalInvestedRemaining`: suma del coste de adquisición de las unidades
 *   que aún quedan (`netQuantity × averageCostOfRemaining`).
 * - `realizedPnL`: ganancia/pérdida acumulada cerrada por las ventas
 *   ejecutadas, calculada lote a lote contra el precio FIFO consumido.
 * - `unrealizedPnL`: ganancia/pérdida latente sobre las unidades que aún
 *   tienes en cartera, valoradas al `currentPrice`. 0 si no hay precio actual.
 * - `totalPnL`: realizedPnL + unrealizedPnL.
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
    val hasCurrentPrice: Boolean
)

/**
 * Lote vivo en cartera: una compra (o el remanente de una compra) cuya
 * cantidad aún no ha sido consumida por ventas posteriores.
 */
data class FifoOpenLot(
    val purchaseTransactionId: String,
    val purchaseDate: Long,
    val pricePerUnit: Double,
    val originalQuantity: Double,
    val remainingQuantity: Double
) {
    /** Coste pendiente del lote (qty restante × precio de compra). */
    val remainingCost: Double get() = remainingQuantity * pricePerUnit
}

/**
 * Trozo de un lote de compra consumido por una venta concreta. Una venta
 * puede generar varias consumiciones si su cantidad excede la del lote
 * más antiguo y por tanto cruza varios lotes.
 */
data class FifoLotConsumption(
    val purchaseTransactionId: String,
    val purchaseDate: Long,
    val purchasePrice: Double,
    val quantityConsumed: Double,
    /** P&L del trozo: (precio venta - precio compra) × cantidad consumida. */
    val pnl: Double
)

/**
 * Trazabilidad FIFO de una venta: precio y cantidad de la venta y la
 * lista de lotes que se consumieron para satisfacerla, con el P&L parcial
 * de cada uno. La suma de los P&L parciales es el P&L realizado por la
 * venta completa.
 */
data class FifoSaleMatch(
    val saleTransactionId: String,
    val saleDate: Long,
    val salePrice: Double,
    val saleQuantity: Double,
    val consumed: List<FifoLotConsumption>,
    val realizedPnL: Double
)

/**
 * Desglose FIFO completo de un activo: el inventario vivo lote a lote y
 * la trazabilidad de cada venta contra los lotes que consumió. Pensado
 * para mostrar al usuario el detalle fiscal de sus operaciones (decisión
 * 1.B sobre fiscalidad española FIFO).
 */
data class FifoBreakdown(
    /** Lotes con remaining > 0; vacío si la posición está cerrada. */
    val openLots: List<FifoOpenLot>,
    /** Ventas con su trazabilidad. Vacío si nunca hubo ventas. */
    val saleMatches: List<FifoSaleMatch>
) {
    val hasAnyData: Boolean get() = openLots.isNotEmpty() || saleMatches.isNotEmpty()
}

/**
 * Calculadora pura de portfolio basada en FIFO. No tiene dependencias de
 * Compose, Koin ni de la BD: trabaja sobre listas de movimientos ya cargadas.
 *
 * Decisión 1.B del rediseño: las ventas consumen los lotes en el orden en
 * que se compraron (primero en entrar, primero en salir). Es coherente con
 * la fiscalidad española y permite calcular el P&L realizado correctamente
 * incluso cuando el usuario tiene varias compras a precios distintos.
 *
 * El campo `feeNote` de los movimientos NO se usa aquí: por la decisión 5.C
 * del rediseño, las comisiones son texto informativo y no afectan al cálculo.
 */
object PortfolioCalculator {

    /**
     * Calcula la posición agregada de un activo a partir de sus movimientos.
     *
     * @param transactions movimientos del activo en cualquier orden; se
     *        reordenarán cronológicamente para aplicar FIFO.
     * @param currentPrice precio actual del activo (NULL si no se conoce
     *        todavía → unrealized = 0, total = realized).
     */
    fun calculate(
        transactions: List<AssetTransaction>,
        currentPrice: Double?
    ): AssetPosition {
        val ordered = transactions.sortedWith(
            compareBy({ it.date }, { it.createdAt })
        )

        // Cola FIFO de lotes pendientes de consumir.
        val lots = ArrayDeque<Lot>()
        var realizedPnL = 0.0

        for (tx in ordered) {
            when (tx.type) {
                AssetTransactionType.BUY  -> lots.addLast(Lot(tx.quantity, tx.pricePerUnit))
                AssetTransactionType.SELL -> realizedPnL += consumeFifo(lots, tx)
            }
        }

        val netQuantity = lots.sumOf { it.qtyRemaining }
        val totalInvestedRemaining = lots.sumOf { it.qtyRemaining * it.pricePerUnit }
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

        val totalPnL = realizedPnL + unrealizedPnL
        // El % total se referencia al total invertido a lo largo de la vida
        // del activo (suma de todas las compras), porque realizedPnL incluye
        // ventas que ya no están en `totalInvestedRemaining`.
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
            hasCurrentPrice        = hasPrice
        )
    }

    /**
     * Cantidad disponible para vender en una fecha concreta a partir del
     * histórico de movimientos. Útil para validar en la UI que el usuario
     * no intente vender más unidades de las que posee (decisión 7.A).
     *
     * El cálculo es FIFO contra los movimientos cuyas fechas son anteriores
     * a `asOfDate` (incluida).
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
     * Devuelve el desglose FIFO lote a lote: qué lotes siguen vivos en
     * cartera tras aplicar todas las ventas, y para cada venta contra qué
     * lotes se cruzó con qué P&L parcial. Es la información que necesita
     * la vista de detalle del activo para que el usuario entienda el
     * cálculo de su beneficio o pérdida (especialmente en activos con
     * varias compras a precios distintos).
     *
     * Se mantiene el mismo orden de consumo que [calculate]: cronológico
     * por (date, createdAt). Es coherente con la decisión 1.B y con la
     * fiscalidad española.
     */
    fun breakdown(transactions: List<AssetTransaction>): FifoBreakdown {
        val ordered = transactions.sortedWith(
            compareBy({ it.date }, { it.createdAt })
        )

        // Lote con trazabilidad: guardamos el id de la compra origen y la
        // cantidad original para poder mostrar "3 de 5 consumidos" en la UI.
        data class TrackedLot(
            val txId: String,
            val date: Long,
            val pricePerUnit: Double,
            val originalQty: Double,
            var remaining: Double
        )

        val lots  = ArrayDeque<TrackedLot>()
        val sales = mutableListOf<FifoSaleMatch>()

        for (tx in ordered) {
            when (tx.type) {
                AssetTransactionType.BUY -> lots.addLast(
                    TrackedLot(
                        txId         = tx.id,
                        date         = tx.date,
                        pricePerUnit = tx.pricePerUnit,
                        originalQty  = tx.quantity,
                        remaining    = tx.quantity
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
                            consumed          = consumed,
                            realizedPnL       = realized
                        )
                    )
                }
            }
        }

        val openLots = lots
            .filter { it.remaining > 0.0 }
            .map {
                FifoOpenLot(
                    purchaseTransactionId = it.txId,
                    purchaseDate          = it.date,
                    pricePerUnit          = it.pricePerUnit,
                    originalQuantity      = it.originalQty,
                    remainingQuantity     = it.remaining
                )
            }

        // Ventas más recientes primero — es lo que más suele consultarse.
        return FifoBreakdown(
            openLots    = openLots,
            saleMatches = sales.sortedByDescending { it.saleDate }
        )
    }

    // ── Internals ─────────────────────────────────────────────────────────

    private data class Lot(var qtyRemaining: Double, val pricePerUnit: Double)

    /** Consume `tx.quantity` desde el frente de la cola de lotes y devuelve
     *  el P&L realizado. Si la venta excede el inventario disponible, se
     *  consume todo lo que haya (la UI ya bloquea esto, pero el cálculo es
     *  defensivo para que un dato corrupto no rompa la app). */
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
