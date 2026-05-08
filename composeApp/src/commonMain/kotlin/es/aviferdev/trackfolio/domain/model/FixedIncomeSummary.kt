package es.aviferdev.trackfolio.domain.model

data class FixedIncomeRow(
    val position: FixedIncomePosition,
    val collectedInterest: Double,
    val currentValue: Double,
    val totalProfit: Double,
    val totalProfitPercent: Double
)

data class FixedIncomeSummary(
    val totalPrincipal: Double,
    val totalCurrentValue: Double,
    val totalAccruedInterest: Double,
    val totalCollectedInterest: Double,
    val totalNetProfit: Double,
    val totalNetProfitPercent: Double,
    val openPositionsCount: Int,
    val nearMaturityCount: Int,
    val positions: List<FixedIncomeRow>
)