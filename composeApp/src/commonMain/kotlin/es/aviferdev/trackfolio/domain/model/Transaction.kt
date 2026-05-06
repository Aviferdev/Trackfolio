package es.aviferdev.trackfolio.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    /** Importe neto (lo que entra/sale realmente de la cuenta). */
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
    val issuerName: String? = null
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
}

enum class TransactionType { INCOME, EXPENSE }
