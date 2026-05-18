package es.aviferdev.n3to.domain.model

enum class LoanType(val label: String, val emoji: String, val resourceKey: String) {
    MORTGAGE("Hipoteca", "🏠", "loan_type_mortgage"),
    CAR("Préstamo coche", "🚗", "loan_type_car"),
    STUDENT("Préstamo estudiantil", "🎓", "loan_type_student"),
    PERSONAL("Préstamo personal", "💰", "loan_type_personal"),
    OTHER("Otro", "📋", "loan_type_other");

    companion object {
        fun fromName(name: String?): LoanType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
