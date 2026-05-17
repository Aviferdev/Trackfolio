package es.aviferdev.n3to.domain.model

/**
 * Modelo de dominio para una propiedad inmobiliaria.
 * V2 — modelo simplificado sin valoraciones, gastos estructurados ni simulación.
 *
 * Las propiedades se valoran manualmente y pueden vincularse a un préstamo
 * hipotecario existente (LoanType.MORTGAGE).
 */
data class RealEstateProperty(
    val id: String,
    val accountId: String,
    val name: String,
    val address: String,
    val propertyType: PropertyType,
    val purchaseValue: Double,
    val currentEstimatedValue: Double,
    val acquisitionDate: Long,
    val ownershipPercentage: Double,     // 0.0 - 100.0
    val linkedLoanId: String?,
    val rentalStatus: RentalStatus,
    val monthlyRent: Double?,
    val mortgageReminderDismissed: Boolean,
    val archived: Boolean,
    // ── Datos de venta ──────────────────────────────────────
    val saleDate: Long? = null,
    val saleValue: Double? = null
) {
    /** Valor efectivo ponderado por el % de propiedad */
    val effectiveValue: Double
        get() = currentEstimatedValue * (ownershipPercentage / 100.0)

    /** Plusvalía latente (no realizada) */
    val unrealizedGain: Double
        get() = effectiveValue - (purchaseValue * ownershipPercentage / 100.0)

    /** Rentabilidad latente en % */
    val unrealizedGainPercent: Double
        get() = if (purchaseValue > 0) {
            (unrealizedGain / (purchaseValue * ownershipPercentage / 100.0)) * 100.0
        } else 0.0

    /** ¿Está actualmente alquilada? */
    val isRented: Boolean
        get() = rentalStatus == RentalStatus.RENTED

    /** Ingreso bruto anual estimado por alquiler */
    val annualGrossRent: Double
        get() = if (isRented && monthlyRent != null) monthlyRent * 12.0 else 0.0

    /** Rentabilidad bruta sobre precio de compra (%) */
    val grossRentalYieldOnPurchase: Double
        get() = if (purchaseValue > 0 && annualGrossRent > 0) {
            (annualGrossRent / purchaseValue) * 100.0
        } else 0.0

    /** Rentabilidad bruta sobre valor actual (%) */
    val grossRentalYieldOnCurrent: Double
        get() = if (currentEstimatedValue > 0 && annualGrossRent > 0) {
            (annualGrossRent / currentEstimatedValue) * 100.0
        } else 0.0

    /** Tiene recordatorio de hipoteca pendiente */
    val hasPendingMortgageReminder: Boolean
        get() = linkedLoanId == null && !mortgageReminderDismissed

    // ── Propiedades de venta ─────────────────────────────────

    /** Indica si la propiedad ha sido vendida (tiene datos de venta y está archivada). */
    val isSold: Boolean
        get() = saleDate != null && saleValue != null && archived

    /** Plusvalía realizada (solo si está vendida). */
    val realizedGain: Double?
        get() {
            if (!isSold || saleValue == null) return null
            val ownership = ownershipPercentage / 100.0
            return (saleValue * ownership) - (purchaseValue * ownership)
        }

    /** Plusvalía realizada en % sobre el precio de compra. */
    val realizedGainPercent: Double?
        get() {
            val gain = realizedGain ?: return null
            val basis = purchaseValue * ownershipPercentage / 100.0
            return if (basis > 0) (gain / basis) * 100.0 else 0.0
        }
}
