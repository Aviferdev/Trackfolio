package es.aviferdev.n3to.domain.model

enum class LoanType(val label: String, val emoji: String) {
    MORTGAGE("Hipoteca", "🏠"),
    CAR("Préstamo coche", "🚗"),
    STUDENT("Préstamo estudiantil", "🎓"),
    PERSONAL("Préstamo personal", "💰"),
    OTHER("Otro", "📋");

    companion object {
        fun fromName(name: String?): LoanType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
