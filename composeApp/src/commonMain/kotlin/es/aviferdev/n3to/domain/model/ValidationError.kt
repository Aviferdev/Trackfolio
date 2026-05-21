package es.aviferdev.n3to.domain.model

/**
 * Errores de validación tipados para reemplazar `require()` en UseCases.
 * Cada subclase representa un error de validación específico con un
 * `message` legible para mostrar en la UI.
 */
sealed class ValidationError(override val message: String) : Throwable(message) {

    // ─── Real Estate ─────────────────────────────────────────────────
    data object PropertyNameEmpty : ValidationError("El nombre no puede estar vacío")
    data object PropertyAddressEmpty : ValidationError("La dirección no puede estar vacía")
    data object PurchaseValueInvalid : ValidationError("El valor de compra debe ser mayor que 0")
    data object EstimatedValueInvalid : ValidationError("El valor estimado debe ser mayor que 0")
    data object AcquisitionDateRequired : ValidationError("La fecha de adquisición es obligatoria")
    data object OwnershipPercentageInvalid :
        ValidationError("El porcentaje de propiedad debe estar entre 0 y 100")

    data object RentalIncomeRequired :
        ValidationError("La renta mensual es obligatoria para propiedades alquiladas")

    data object SalePriceInvalid : ValidationError("El precio de venta debe ser mayor que 0")
    data object SaleDateRequired : ValidationError("La fecha de venta es obligatoria")
    data object ExpenseAmountInvalid : ValidationError("El importe del gasto debe ser mayor que 0")

    // ─── Fixed Income ────────────────────────────────────────────────
    data object FixedIncomeNameEmpty : ValidationError("El nombre no puede estar vacío")
    data object FixedIncomePrincipalInvalid : ValidationError("El capital debe ser mayor que 0")
    data object FixedIncomeInterestRateInvalid :
        ValidationError("El tipo de interés debe ser mayor que 0")

    data object FixedIncomeDurationInvalid : ValidationError("La duración debe ser mayor que 0")
    data object FixedIncomeIssuerRequired : ValidationError("Debes seleccionar un emisor")

    // ─── Portfolio / Assets ──────────────────────────────────────────
    data object AssetNameEmpty : ValidationError("El nombre del activo no puede estar vacío")
    data object AssetTickerEmpty : ValidationError("El ticker no puede estar vacío")
    data object AssetCategoryRequired : ValidationError("Debes seleccionar una categoría")
    data object QuantityInvalid : ValidationError("La cantidad debe ser mayor que 0")
    data object PriceInvalid : ValidationError("El precio debe ser mayor que 0")

    // ─── Loan / Debt ─────────────────────────────────────────────────
    data object LoanNameEmpty : ValidationError("El nombre del préstamo no puede estar vacío")
    data object LoanAmountInvalid : ValidationError("El capital debe ser mayor que 0")
    data object LoanInterestInvalid : ValidationError("El tipo de interés no puede ser negativo")
    data object LoanTermInvalid : ValidationError("El plazo debe ser mayor que 0")

    // ─── Account ─────────────────────────────────────────────────────
    data object AccountNameEmpty : ValidationError("El nombre de la cuenta no puede estar vacío")

    // ─── Valuable (Bienes) ───────────────────────────────────────────
    data object ValuableNameEmpty : ValidationError("El nombre del bien no puede estar vacío")
    data object ValuablePurchasePriceInvalid :
        ValidationError("El precio de compra debe ser mayor que 0")

    data object ValuablePurchaseDateRequired : ValidationError("La fecha de compra es obligatoria")
    data object ValuableSalePriceInvalid :
        ValidationError("El precio de venta debe ser mayor que 0")

    data object ValuableSaleDateRequired : ValidationError("La fecha de venta es obligatoria")
    data object ValuableExpenseAmountInvalid :
        ValidationError("El importe del gasto debe ser mayor que 0")

    // ─── Backup / Reminders ──────────────────────────────────────────
    data class InvalidInterval(val days: Int) : ValidationError("Intervalo no válido: $days")
    data class Custom(val overrideMessage: String) : ValidationError(overrideMessage)
}
