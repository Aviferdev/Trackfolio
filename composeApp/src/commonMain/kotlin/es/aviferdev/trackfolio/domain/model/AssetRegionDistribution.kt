package es.aviferdev.trackfolio.domain.model

data class AssetRegionDistribution(
    val assetId: String,
    val regionId: String,
    val percent: Int
)