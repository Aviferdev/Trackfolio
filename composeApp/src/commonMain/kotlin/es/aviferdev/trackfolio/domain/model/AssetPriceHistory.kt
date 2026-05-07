package es.aviferdev.trackfolio.domain.model

data class AssetPriceHistory(
    val id: String,
    val assetId: String,
    val price: Double,
    val recordedAt: Long
)
