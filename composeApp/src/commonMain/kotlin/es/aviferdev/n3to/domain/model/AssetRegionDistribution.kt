package es.aviferdev.n3to.domain.model

data class AssetRegionDistribution(
    val assetId: String,
    val regionId: String,
    val percent: Int
)