package es.aviferdev.n3to.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    /** Importe neto (lo que entra/sale realmente de la cuenta).
     *  Para ADJUSTMENT: positivo = saldo real mayor, negativo = saldo real menor. */
    val amount: Double,
    val type: TransactionType,
    /** Categoría de gasto. Null para ingresos (usan [incomeType]). */
    val categoryId: String?,
    val date: Long,
    val notes: String?,
    val createdAt: Long,

    // ── Campos de ingreso ─────────────────────────────────────────────────────
    /** Tipo de ingreso. Null para gastos. */
    val incomeType: IncomeType? = null,
    /** Importe bruto antes de deducciones. Null si solo se registró neto. */
    val grossAmount: Double? = null,
    /** Comisiones aplicadas. Solo para BOND_DEPOSIT. */
    val commissionAmount: Double? = null,
    /** ID de la entidad emisora. */
    val issuerId: String? = null,
    /** Nombre desnormalizado del emisor para queries rápidas. */
    val issuerName: String? = null,
    /** Líneas fiscales (impuestos, cotizaciones...). Vacío = solo neto registrado. */
    val taxLines: List<TaxLine> = emptyList(),

    // ── Divisa original (si difiere de la divisa de la cuenta) ───────────────
    /** Código ISO de la divisa original (ej. "USD"). Null si coincide con la cuenta. */
    val originalCurrency: String? = null,
    /** Importe en la divisa original antes de conversión. */
    val originalAmount: Double? = null,
    /** Tipo de cambio aplicado: 1 originalCurrency = exchangeRate accountCurrency. */
    val exchangeRate: Double? = null,

    // ── Vínculo con portfolio ─────────────────────────────────────────────────
    val linkedAssetTransactionId: String? = null,

    // ── Vínculo con préstamo ──────────────────────────────────────────────────
    val linkedLoanId: String? = null,

    // ── Vínculo con propiedad inmobiliaria ────────────────────────────────────
    val linkedPropertyId: String? = null,

    // ── Reconciliación ────────────────────────────────────────────────────────
    /** Si true, esta transacción se excluye del informe fiscal. */
    val excludeFromFiscal: Boolean = false
) {
    /** True si tiene desglose fiscal completo (modo bruto). */
    val hasFiscalBreakdown: Boolean get() = grossAmount != null && taxLines.isNotEmpty()

    /** True si se registró solo el importe neto, sin desglose. */
    val isNetOnly: Boolean get() = !hasFiscalBreakdown

    /** True si este ingreso fue en una divisa distinta a la de la cuenta. */
    val hasCurrencyConversion: Boolean get() = originalCurrency != null

    val incomeLabel: String
        get() = incomeType?.label ?: categoryId ?: "Ingreso"

    val isIncome: Boolean get() = type == TransactionType.INCOME
    val isExpense: Boolean get() = type == TransactionType.EXPENSE
    val isAdjustment: Boolean get() = type == TransactionType.ADJUSTMENT

    val isLinkedToAsset: Boolean get() = linkedAssetTransactionId != null
    val isLinkedToLoan: Boolean get() = linkedLoanId != null
}

enum class TransactionType { INCOME, EXPENSE, ADJUSTMENT }
