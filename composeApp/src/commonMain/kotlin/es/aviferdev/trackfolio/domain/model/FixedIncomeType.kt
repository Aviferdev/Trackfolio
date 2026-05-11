package es.aviferdev.trackfolio.domain.model

enum class FixedIncomeType(val label: String, val emoji: String) {
    DEPOSIT("Depósito", "🏦"),
    BOND("Bono", "📜"),
    BILL("Letra", "📋")
}