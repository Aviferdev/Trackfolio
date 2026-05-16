package es.aviferdev.n3to.domain.model

data class AssetPriceHistory(
    val id: String,
    val assetId: String,
    val price: Double,
    val recordedAt: Long
)
