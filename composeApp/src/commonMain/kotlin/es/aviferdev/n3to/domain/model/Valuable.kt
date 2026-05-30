package es.aviferdev.n3to.domain.model

/**
 * Modelo de dominio para un bien mueble de valor.
 * Representa un activo físico (coche, maquinaria, obra de arte, etc.)
 * que se adquiere, puede revalorizarse o depreciarse, y puede venderse.
 *
 * Cada bien es único e indivisible (no aplica FIFO).
 * Los gastos asociados se registran como transacciones vinculadas
 * (Transaction.linkedValuableId) y se tienen en cuenta en el cálculo
 * de beneficio neto.
 *
 * @property id Identificador único del bien.
 * @property accountId Cuenta a la que pertenece.
 * @property name Nombre descriptivo del bien.
 * @property description Descripción detallada (opcional).
 * @property purchasePrice Precio de adquisición.
 * @property purchaseDate Fecha de compra (epoch millis).
 * @property estimatedValue Valor estimado actual (opcional). Si no se define,
 *                           se usa purchasePrice para el cálculo de patrimonio.
 * @property salePrice Precio de venta. Null si no se ha vendido.
 * @property saleDate Fecha de venta. Null si no se ha vendido.
 * @property linkedLoanId Préstamo vinculado a este bien (opcional).
 * @property notes Notas adicionales.
 * @property archived True si el bien está archivado (oculto, no eliminado).
 * @property createdAt Fecha de creación del registro (epoch millis).
 */
data class Valuable(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String = "",
    val purchasePrice: Double,
    val purchaseDate: Long,
    val estimatedValue: Double? = null,
    val salePrice: Double? = null,
    val saleDate: Long? = null,
    val linkedLoanId: String? = null,
    val notes: String? = null,
    val archived: Boolean = false,
    val createdAt: Long
) {
    // ── Propiedades calculadas ──────────────────────────────────────

    /** Indica si el bien ha sido vendido. */
    val isSold: Boolean
        get() = saleDate != null && salePrice != null

    /** Valor actual del bien.
     *  - Si está vendido: precio de venta.
     *  - Si tiene valor estimado manual: ese valor.
     *  - Si no: precio de compra (conservador). */
    val currentValue: Double
        get() = salePrice ?: (estimatedValue ?: purchasePrice)

    /** Beneficio bruto (precio venta − precio compra).
     *  Null si el bien no se ha vendido aún. */
    val grossProfit: Double?
        get() = if (isSold && salePrice != null) salePrice - purchasePrice else null

    /** Rentabilidad bruta sobre precio de compra (%). */
    val grossProfitPercent: Double?
        get() {
            val profit = grossProfit ?: return null
            return if (purchasePrice > 0) (profit / purchasePrice) * 100.0 else 0.0
        }
}
