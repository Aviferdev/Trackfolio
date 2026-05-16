package es.aviferdev.n3to.domain.model

/** Tipo de movimiento de portfolio: compra (BUY), venta (SELL) o traspaso entre fondos. */
enum class AssetTransactionType { BUY, SELL, TRANSFER_OUT, TRANSFER_IN }

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

    val isBuy: Boolean         get() = type == AssetTransactionType.BUY
    val isSell: Boolean        get() = type == AssetTransactionType.SELL
    val isTransferOut: Boolean get() = type == AssetTransactionType.TRANSFER_OUT
    val isTransferIn: Boolean  get() = type == AssetTransactionType.TRANSFER_IN
    val isTransfer: Boolean    get() = isTransferOut || isTransferIn

    /**
     * ID del traspaso al que pertenece este movimiento (tanto el OUT como el IN
     * comparten el mismo transferGroupId). Null si no es un traspaso.
     * Se almacena en [notes] con prefijo "TRANSFER:" para no añadir columna a BBDD.
     */
    val transferGroupId: String?
        get() = notes?.takeIf { it.startsWith("TRANSFER:") }?.removePrefix("TRANSFER:")
}
