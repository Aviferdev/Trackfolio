package es.aviferdev.n3to.domain.model

enum class FixedIncomeEventType(val label: String, val resourceKey: String) {
    ACQUISITION("Adquisición", "fixedincome_event_acquisition"),
    COUPON("Cupón / Interés periódico", "fixedincome_event_coupon"),
    MATURITY_SETTLEMENT("Liquidación por vencimiento", "fixedincome_event_maturity_settlement"),
    SECONDARY_SALE("Venta en secundario", "fixedincome_event_secondary_sale"),
    EARLY_CANCELLATION("Cancelación anticipada", "fixedincome_event_early_cancellation"),
    PARTIAL_AMORTIZATION("Amortización parcial", "fixedincome_event_partial_amortization")
}