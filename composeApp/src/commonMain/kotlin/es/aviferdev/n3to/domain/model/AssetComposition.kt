package es.aviferdev.n3to.domain.model

data class AssetComposition(
    val assetId: String,
    val fixedIncomePercent: Int,
    val createdAt: Long
) {
    val fixedIncomePercentDisplay: String get() = "$fixedIncomePercent%"
    val variableIncomePercent: Int get() = 100 - fixedIncomePercent
}