package es.aviferdev.trackfolio.domain.model

enum class InterestFrequency(val label: String) {
    AT_MATURITY("Al vencimiento"),
    MONTHLY("Mensual"),
    QUARTERLY("Trimestral"),
    SEMIANNUAL("Semestral"),
    ANNUAL("Anual")
}