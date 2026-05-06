package es.aviferdev.trackfolio.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    /** Importe neto (lo que entra/sale realmente de la cuenta). */
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val date: Long,
    val notes: String?,
    val createdAt: Long,

    // ── Campos fiscales — solo relevantes para ingresos (type == INCOME) ──────
    /** Importe bruto antes de la retención de IRPF. Null si no se ha introducido info fiscal. */
    val grossAmount: Double? = null,
    /** Porcentaje de retención de IRPF aplicado (ej. 19.0). Null si no procede. */
    val irpfPercent: Double? = null,
    /** Tipo de rendimiento a efectos del IRPF. Null si no se ha clasificado. */
    val taxType: IncomeTaxType? = null
) {
    /** Importe retenido = bruto − neto. Solo significativo si [grossAmount] no es null. */
    val irpfAmount: Double?
        get() = if (grossAmount != null) grossAmount - amount else null
}

enum class TransactionType { INCOME, EXPENSE }
