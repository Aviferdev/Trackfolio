package es.aviferdev.trackfolio.domain.model

/** Tipo de movimiento de portfolio: compra (BUY) o venta (SELL). */
enum class AssetTransactionType { BUY, SELL }

/**
 * Movimiento individual de un activo: una compra o venta puntual.
 * El P&L del activo se deriva agregando todos sus movimientos en orden
 * cronológico (FIFO).
 *
 * @property quantity unidades del activo (siempre positivo; el sentido se
 *           determina por [type]).
 * @property pricePerUnit precio unitario al que se ejecutó el movimiento.
 * @property date fecha en epoch millis en la que se ejecutó (la introduce
 *           el usuario, puede ser pasada).
 * @property feeNote texto libre informativo sobre la comisión. NO entra en
 *           cálculos de coste base ni P&L.
 */
data class AssetTransaction(
    val id: String,
    val assetId: String,
    val type: AssetTransactionType,
    val quantity: Double,
    val pricePerUnit: Double,
    val date: Long,
    val platformId: String,
    val feeNote: String? = null,
    val notes: String? = null,
    val createdAt: Long
) {
    /** Importe total del movimiento sin comisiones (cantidad × precio unitario). */
    val grossAmount: Double get() = quantity * pricePerUnit

    val isBuy: Boolean  get() = type == AssetTransactionType.BUY
    val isSell: Boolean get() = type == AssetTransactionType.SELL
}
