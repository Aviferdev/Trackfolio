package es.aviferdev.trackfolio.domain.model

/**
 * Catálogo del activo: ficha de identidad. Los datos de cuántas unidades
 * tienes y a qué precio las compraste viven en [AssetTransaction].
 *
 * `currentPrice` representa la cotización actual editable por el usuario;
 * mientras sea NULL, los cálculos de P&L caen en 0 ("Sin precio actual").
 *
 * `archived`: los activos NUNCA se borran, solo se archivan. Un activo
 * archivado no aparece en listados normales pero sus transacciones y
 * P&L histórico se conservan.
 */
data class Asset(
    val id: String,
    val accountId: String,
    val ticker: String,
    val name: String,
    val notes: String?,
    val createdAt: Long,
    val assetCategoryId: String? = null,
    val currentPrice: Double? = null,
    val currentPriceUpdatedAt: Long? = null,
    val archived: Boolean = false
) {
    val hasCurrentPrice: Boolean get() = currentPrice != null
}
