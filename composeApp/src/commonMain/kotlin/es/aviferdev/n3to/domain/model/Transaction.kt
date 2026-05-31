package es.aviferdev.n3to.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    /** Importe neto (lo que entra/sale realmente de la cuenta).
     *  Para ADJUSTMENT: positivo = saldo real mayor, negativo = saldo real menor. */
    val amount: Double,
    val type: TransactionType,
    /** Categoría de gasto. Null para ingresos (usan [incomeType] vía [taxDetails]). */
    val categoryId: String?,
    val date: Long,
    val notes: String?,
    val createdAt: Long,

    // ── Detalles fiscales (solo para INCOME) ───────────────────────────────
    /**
     * Detalles fiscales de la transacción de ingreso.
     * Null para gastos y ajustes. Contiene incomeType, grossAmount, commissionAmount, issuerId.
     */
    val taxDetails: IncomeTaxDetails? = null,

    /** Líneas fiscales (impuestos, cotizaciones...). Vacío = solo neto registrado. */
    val taxLines: List<TaxLine> = emptyList(),

    // ── Divisa original (si difiere de la divisa de la cuenta) ───────────────
    /** Código ISO de la divisa original (ej. "USD"). Null si coincide con la cuenta. */
    val originalCurrency: String? = null,
    /** Importe en la divisa original antes de conversión. */
    val originalAmount: Double? = null,
    /** Tipo de cambio aplicado: 1 originalCurrency = exchangeRate accountCurrency. */
    val exchangeRate: Double? = null,

    // ── Vínculos con otras entidades (reemplaza linked* fields) ──────────────
    val links: List<TransactionLink> = emptyList(),

    // ── Reconciliación ────────────────────────────────────────────────────────
    /** Si true, esta transacción se excluye del informe fiscal. */
    val excludeFromFiscal: Boolean = false
) {
    /** Tipo de ingreso. Null para gastos. Acceso directo desde taxDetails. */
    val incomeType: IncomeType? get() = taxDetails?.incomeType

    /** Importe bruto antes de deducciones. Null si solo se registró neto. */
    val grossAmount: Double? get() = taxDetails?.grossAmount

    /** Comisiones aplicadas. Solo para BOND_DEPOSIT. */
    val commissionAmount: Double? get() = taxDetails?.commissionAmount

    /** ID de la entidad emisora. FK a IssuerEntity. */
    val issuerId: String? get() = taxDetails?.issuerId

    /** Nombre del emisor (transitorio, no persistido). */
    val issuerName: String? get() = taxDetails?.issuerName

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

    // ── Helpers de compatibilidad (derivados de links) ───────────────────────

    val isLinkedToAsset: Boolean
        get() = links.any { it.linkType == TransactionLinkType.ASSET_TRANSACTION }

    val isLinkedToLoan: Boolean
        get() = links.any { it.linkType == TransactionLinkType.LOAN }

    val isLinkedToValuable: Boolean
        get() = links.any { it.linkType == TransactionLinkType.VALUABLE }

    val isLinkedToProperty: Boolean
        get() = links.any { it.linkType == TransactionLinkType.PROPERTY }

    val linkedAssetTransactionId: String?
        get() = links.find { it.linkType == TransactionLinkType.ASSET_TRANSACTION }?.linkedEntityId

    val linkedLoanId: String?
        get() = links.find { it.linkType == TransactionLinkType.LOAN }?.linkedEntityId

    val linkedPropertyId: String?
        get() = links.find { it.linkType == TransactionLinkType.PROPERTY }?.linkedEntityId

    val linkedValuableId: String?
        get() = links.find { it.linkType == TransactionLinkType.VALUABLE }?.linkedEntityId

    val linkedDividendId: String?
        get() = links.find { it.linkType == TransactionLinkType.DIVIDEND }?.linkedEntityId

    val linkedBondDepositId: String?
        get() = links.find { it.linkType == TransactionLinkType.BOND_DEPOSIT }?.linkedEntityId
}

enum class TransactionType { INCOME, EXPENSE, ADJUSTMENT }
