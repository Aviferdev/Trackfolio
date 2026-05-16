package es.aviferdev.n3to.domain.model

enum class InterestFrequency(val label: String) {
    AT_MATURITY("Al vencimiento"),
    MONTHLY("Mensual"),
    QUARTERLY("Trimestral"),
    SEMIANNUAL("Semestral"),
    ANNUAL("Anual")
}