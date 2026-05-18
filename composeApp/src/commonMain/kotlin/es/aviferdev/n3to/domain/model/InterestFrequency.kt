package es.aviferdev.n3to.domain.model

enum class InterestFrequency(val label: String, val resourceKey: String) {
    AT_MATURITY("Al vencimiento", "interest_frequency_at_maturity"),
    MONTHLY("Mensual", "interest_frequency_monthly"),
    QUARTERLY("Trimestral", "interest_frequency_quarterly"),
    SEMIANNUAL("Semestral", "interest_frequency_semiannual"),
    ANNUAL("Anual", "interest_frequency_annual")
}