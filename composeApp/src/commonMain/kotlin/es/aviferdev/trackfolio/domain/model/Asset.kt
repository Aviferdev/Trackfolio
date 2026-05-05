package es.aviferdev.trackfolio.domain.model

data class Asset(
    val id: String,
    val accountId: String,
    val ticker: String,           // símbolo o identificador (ej. "AAPL", "BTC", "IAG.MC")
    val name: String,             // nombre descriptivo (ej. "Apple Inc.", "Bitcoin")
    val quantity: Double,         // número de unidades / acciones
    val purchasePrice: Double,    // precio unitario de compra
    val purchaseDate: Long,       // epoch millis
    val notes: String?,
    val createdAt: Long,
    val assetCategoryId: String? = null   // FK opcional a AssetCategory
) {
    /** Valor total invertido = cantidad × precio de compra */
    val totalInvested: Double get() = quantity * purchasePrice

    /**
     * Ganancia/pérdida con precio actual.
     * Se calcula en el ViewModel una vez que se tiene el precio actual.
     */
    fun unrealizedPnL(currentPrice: Double): Double =
        (currentPrice - purchasePrice) * quantity

    fun pnLPercent(currentPrice: Double): Double =
        if (purchasePrice == 0.0) 0.0
        else ((currentPrice - purchasePrice) / purchasePrice) * 100.0

    fun currentValue(currentPrice: Double): Double = currentPrice * quantity
}
