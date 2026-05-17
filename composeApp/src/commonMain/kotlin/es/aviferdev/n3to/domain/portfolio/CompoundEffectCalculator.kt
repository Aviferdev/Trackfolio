package es.aviferdev.n3to.domain.portfolio

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Transaction
import kotlin.math.max
import kotlin.math.pow

/**
 * Datos de entrada para una posición individual en el cálculo del efecto compuesto.
 * @property asset  Ficha del activo.
 * @property transactions  Todas las transacciones del activo (para FIFO y coste histórico).
 * @property currentPrice  Precio actual del activo (puede ser null → no calculable).
 * @property dividends  Dividendos cobrados asociados a este activo.
 */
data class PositionInput(
    val asset: Asset,
    val transactions: List<AssetTransaction>,
    val currentPrice: Double?,
    val dividends: List<Transaction>
)

/**
 * Efecto compuesto de una posición individual.
 * @property totalInvested  Coste total histórico (suma de todas las compras y transferencias).
 * @property currentValue  Valor de mercado actual (netQuantity × currentPrice).
 * @property dividendIncome  Suma de dividendos cobrados.
 * @property totalValue  currentValue + dividendIncome (valor total asumiendo reinversión).
 * @property yearsInvested  Periodo medio ponderado en años.
 * @property cagr  Tasa de crecimiento anual compuesta (en %, ej: 12.5 para 12.5%).
 * @property simpleReturnValue  Valor hipotético si los beneficios se retiraran cada año.
 * @property compoundEffect  totalValue - simpleReturnValue (el "extra" compuesto, mínimo 0).
 */
data class PositionCompoundEffect(
    val assetId: String,
    val ticker: String,
    val name: String,
    val totalInvested: Double,
    val currentValue: Double,
    val dividendIncome: Double,
    val totalValue: Double,
    val yearsInvested: Double,
    val cagr: Double,
    val simpleReturnValue: Double,
    val compoundEffect: Double
)

/**
 * Efecto compuesto agregado del portfolio.
 * @property totalInvested  Suma de costes totales de todas las posiciones contribuyentes.
 * @property totalValue  Suma de valores totales (mercado + dividendos).
 * @property totalReturn  totalValue - totalInvested.
 * @property simpleReturnValue  Suma de valores hipotéticos sin reinversión.
 * @property compoundEffect  totalValue - simpleReturnValue.
 * @property compoundEffectPercent  Porcentaje del retorno total que es efecto compuesto.
 * @property positionCount  Número de posiciones que contribuyen al cálculo.
 * @property avgCagr  Tasa de crecimiento anual compuesta media ponderada (en %).
 * @property positions  Desglose por posición individual.
 */
data class CompoundEffect(
    val totalInvested: Double,
    val totalValue: Double,
    val totalReturn: Double,
    val simpleReturnValue: Double,
    val compoundEffect: Double,
    val compoundEffectPercent: Double,
    val positionCount: Int,
    val avgCagr: Double,
    val positions: List<PositionCompoundEffect>
)

/**
 * Calculadora del efecto compuesto de la cartera.
 *
 * Compara el valor real con reinversión (precio actual × cantidad + dividendos)
 * frente a un escenario hipotético "sin reinversión" donde cada año se retiran
 * los beneficios y solo se mantiene el capital original.
 *
 * La diferencia entre ambos escenarios es el **efecto compuesto**: el "extra"
 * que genera el interés compuesto al no retirar los beneficios.
 *
 * **Nota:** Solo aplica a renta variable. La renta fija se excluye porque sus
 * cupones se pagan en efectivo y no se auto-invierten.
 */
object CompoundEffectCalculator {

    private const val MILLIS_PER_YEAR = 365.25 * 86_400_000.0
    private const val MIN_YEARS = 0.25          // 3 meses mínimo para considerar
    private const val MAX_CAGR = 10.0           // 1000% máximo para evitar absurdos

    /**
     * Calcula el efecto compuesto agregado a partir de una lista de posiciones.
     * @param positions  Lista de [PositionInput] con los datos de cada activo.
     * @param nowMillis  Timestamp actual para calcular periodos.
     * @param minYears  Periodo mínimo en años (default 0.25 = 3 meses).
     * @return [CompoundEffect] agregado, o null si ninguna posición es calculable.
     */
    fun calculate(
        positions: List<PositionInput>,
        nowMillis: Long,
        minYears: Double = MIN_YEARS
    ): CompoundEffect? {
        val positionEffects = positions.mapNotNull { input ->
            calculatePosition(input, nowMillis, minYears)
        }
        if (positionEffects.isEmpty()) return null

        val totalInvested = positionEffects.sumOf { it.totalInvested }
        val totalValue = positionEffects.sumOf { it.totalValue }
        val totalReturn = totalValue - totalInvested
        val simpleReturnValue = positionEffects.sumOf { it.simpleReturnValue }
        val compoundEffect = max(0.0, positionEffects.sumOf { it.compoundEffect })
        val compoundEffectPercent = if (totalReturn > 0.0) {
            (compoundEffect / totalReturn) * 100.0
        } else 0.0

        // CAGR media ponderada por capital invertido
        val avgCagr = if (totalInvested > 0.0) {
            positionEffects.sumOf { it.cagr * it.totalInvested } / totalInvested
        } else 0.0

        return CompoundEffect(
            totalInvested = totalInvested,
            totalValue = totalValue,
            totalReturn = totalReturn,
            simpleReturnValue = simpleReturnValue,
            compoundEffect = compoundEffect,
            compoundEffectPercent = compoundEffectPercent,
            positionCount = positionEffects.size,
            avgCagr = avgCagr,
            positions = positionEffects
        )
    }

    /**
     * Calcula el efecto compuesto para una posición individual.
     * @return [PositionCompoundEffect] o null si la posición no cumple los criterios.
     */
    private fun calculatePosition(
        input: PositionInput,
        nowMillis: Long,
        minYears: Double
    ): PositionCompoundEffect? {
        val currentPrice = input.currentPrice ?: return null

        // Calcular posición FIFO para obtener cantidad neta
        val position = PortfolioCalculator.calculate(
            transactions = input.transactions,
            currentPrice = currentPrice
        )
        if (position.netQuantity <= 0.0) return null

        // Calcular coste total histórico (todas las compras + transferencias entrantes)
        val purchases = input.transactions.filter {
            it.type == AssetTransactionType.BUY || it.type == AssetTransactionType.TRANSFER_IN
        }
        if (purchases.isEmpty()) return null

        val totalInvested = purchases.sumOf { it.quantity * it.pricePerUnit }
        if (totalInvested <= 0.0) return null

        val marketValue = position.currentValue
        val dividendIncome = input.dividends.sumOf { it.amount }
        val totalValue = marketValue + dividendIncome

        // Periodo medio ponderado por importe
        val weightedYears = weightedAverageYears(purchases, nowMillis)
        if (weightedYears < minYears) return null

        // Si hay pérdidas → no hay efecto compuesto positivo
        if (totalValue <= totalInvested) {
            return PositionCompoundEffect(
                assetId = input.asset.id,
                ticker = input.asset.ticker,
                name = input.asset.name,
                totalInvested = totalInvested,
                currentValue = marketValue,
                dividendIncome = dividendIncome,
                totalValue = totalValue,
                yearsInvested = weightedYears,
                cagr = 0.0,
                simpleReturnValue = totalValue,
                compoundEffect = 0.0
            )
        }

        // CAGR = (totalValue / totalInvested)^(1/años) - 1
        val rawCagr = (totalValue / totalInvested).pow(1.0 / weightedYears) - 1.0
        val cagr = rawCagr.coerceIn(0.0, MAX_CAGR)

        // Valor con rendimiento simple: capital × (1 + CAGR × años)
        val simpleReturnValue = totalInvested * (1.0 + cagr * weightedYears)

        // Efecto compuesto = valor real - valor hipotético simple
        val compoundEffect = max(0.0, totalValue - simpleReturnValue)

        return PositionCompoundEffect(
            assetId = input.asset.id,
            ticker = input.asset.ticker,
            name = input.asset.name,
            totalInvested = totalInvested,
            currentValue = marketValue,
            dividendIncome = dividendIncome,
            totalValue = totalValue,
            yearsInvested = weightedYears,
            cagr = cagr * 100.0,  // Devolvemos en porcentaje
            simpleReturnValue = simpleReturnValue,
            compoundEffect = compoundEffect
        )
    }

    /**
     * Calcula el periodo medio ponderado en años desde las compras.
     * Pondera por importe (quantity × pricePerUnit) para dar más peso
     * a las compras grandes.
     */
    private fun weightedAverageYears(
        purchases: List<AssetTransaction>,
        nowMillis: Long
    ): Double {
        var totalWeight = 0.0
        var totalYears = 0.0
        for (tx in purchases) {
            val weight = tx.quantity * tx.pricePerUnit
            if (weight <= 0.0) continue
            val years = (nowMillis - tx.date) / MILLIS_PER_YEAR
            totalWeight += weight
            totalYears += years * weight
        }
        return if (totalWeight > 0.0) totalYears / totalWeight else 0.0
    }
}
