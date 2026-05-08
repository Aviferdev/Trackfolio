package es.aviferdev.trackfolio.domain.model

enum class FixedIncomeType(val label: String, val emoji: String) {
    BOND("Bono / Deuda pública", "📜"),
    DEPOSIT("Depósito bancario", "🏦")
}