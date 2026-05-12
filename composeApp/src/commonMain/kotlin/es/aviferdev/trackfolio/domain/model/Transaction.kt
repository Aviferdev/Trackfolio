package es.aviferdev.trackfolio.domain.model

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
    /** Importe bruto antes de retenciones. Null si no aplica. */
    val grossAmount: Double? = null,
    /** Porcentaje de retención de IRPF (ej. 19.0). Null si no aplica. */
    val irpfPercent: Double? = null,
    /** Cotizaciones a la Seguridad Social. Solo para SALARY. */
    val socialSecurityAmount: Double? = null,
    /** Comisiones aplicadas. Solo para BOND_DEPOSIT. */
    val commissionAmount: Double? = null,
    /** ID de la entidad emisora. Null si no aplica (EXEMPT o gastos). */
    val issuerId: String? = null,
    /** Nombre desnormalizado del emisor para queries rápidas. */
    val issuerName: String? = null,

    // ── Vínculo con portfolio ─────────────────────────────────────────────────
    /** ID de la AssetTransaction vinculada. Si != null, este movimiento es
     *  de solo lectura — se gestiona desde Portfolio. */
    val linkedAssetTransactionId: String? = null,

    // ── Vínculo con préstamo ────────────────────────────────────────────────────
    /** ID del préstamo vinculado. Si != null, este gasto es una cuota de préstamo. */
    val linkedLoanId: String? = null,

    // ── Reconciliación ────────────────────────────────────────────────────────
    /** Si true, esta transacción se excluye del informe fiscal (IRPF). */
    val excludeFromFiscal: Boolean = false,
    /** Si true, solo se registró el neto (sin desglose fiscal). */
    val isNetOnlyIncome: Boolean = false
) {
    /** Importe retenido por IRPF = bruto − cotizaciones − comisiones − neto. */
    val irpfAmount: Double?
        get() {
            if (grossAmount == null) return null
            val gross = grossAmount
            val pct = irpfPercent ?: return null
            return gross * pct / 100.0
        }

    /** Label descriptivo para mostrar en listados de ingresos. */
    val incomeLabel: String
        get() = incomeType?.label ?: categoryId ?: "Ingreso"

    val isIncome: Boolean get() = type == TransactionType.INCOME
    val isExpense: Boolean get() = type == TransactionType.EXPENSE
    val isAdjustment: Boolean get() = type == TransactionType.ADJUSTMENT

    /** True si este movimiento está vinculado a una inversión del portfolio. */
    val isLinkedToAsset: Boolean get() = linkedAssetTransactionId != null

    /** True si este gasto está vinculado a un préstamo. */
    val isLinkedToLoan: Boolean get() = linkedLoanId != null
}

enum class TransactionType { INCOME, EXPENSE, ADJUSTMENT }
