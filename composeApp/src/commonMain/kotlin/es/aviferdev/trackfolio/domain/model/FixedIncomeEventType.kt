package es.aviferdev.trackfolio.domain.model

enum class FixedIncomeEventType(val label: String) {
    ACQUISITION("Adquisición"),
    COUPON("Cupón / Interés periódico"),
    MATURITY_SETTLEMENT("Liquidación por vencimiento"),
    SECONDARY_SALE("Venta en secundario"),
    EARLY_CANCELLATION("Cancelación anticipada"),
    PARTIAL_AMORTIZATION("Amortización parcial")
}