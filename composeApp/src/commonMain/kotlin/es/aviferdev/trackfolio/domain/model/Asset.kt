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
    val assetCategoryId: String? = null,    // FK opcional a AssetCategory
    /**
     * Precio unitario actual del activo. NULL mientras el usuario no haya
     * introducido todavía un precio actualizado. Cuando es NULL, los cálculos
     * de revalorización caen en `purchasePrice` y P&L = 0.
     */
    val currentPrice: Double? = null,
    /** Epoch millis con la fecha en que se actualizó por última vez `currentPrice`. */
    val currentPriceUpdatedAt: Long? = null
) {
    /** Valor total invertido = cantidad × precio de compra */
    val totalInvested: Double get() = quantity * purchasePrice

    /** Precio efectivo a usar en cálculos: el actual si existe, si no el de compra. */
    val effectivePrice: Double get() = currentPrice ?: purchasePrice

    /** True si el usuario ya ha introducido un precio actual. */
    val hasCurrentPrice: Boolean get() = currentPrice != null

    /** Valor de mercado actual (cantidad × precio efectivo). */
    val effectiveCurrentValue: Double get() = quantity * effectivePrice

    /** Ganancia/pérdida no realizada con el precio actual conocido (0 si no hay). */
    val effectivePnL: Double get() =
        if (currentPrice == null) 0.0
        else (currentPrice - purchasePrice) * quantity

    /** Porcentaje de revalorización con el precio actual conocido (0 si no hay). */
    val effectivePnLPercent: Double get() =
        if (currentPrice == null || purchasePrice == 0.0) 0.0
        else ((currentPrice - purchasePrice) / purchasePrice) * 100.0

    // ── Helpers paramétricos (compatibilidad con código existente) ───────────
    fun unrealizedPnL(price: Double): Double =
        (price - purchasePrice) * quantity

    fun pnLPercent(price: Double): Double =
        if (purchasePrice == 0.0) 0.0
        else ((price - purchasePrice) / purchasePrice) * 100.0

    fun currentValue(price: Double): Double = price * quantity
}
