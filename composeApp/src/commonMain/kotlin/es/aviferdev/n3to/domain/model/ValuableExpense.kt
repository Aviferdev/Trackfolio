package es.aviferdev.n3to.domain.model

/**
 * Representa un gasto asociado a la compra, tenencia o venta de un bien.
 * Cada gasto tiene una categoría específica para identificar su naturaleza
 * (transporte, reparación, comisión, almacenaje, etc.).
 *
 * @property categoryId Identificador de la categoría del gasto.
 * @property amount Importe del gasto (siempre positivo).
 * @property notes Notopcional sobre el gasto.
 */
data class ValuableExpense(
    val categoryId: String,
    val amount: Double,
    val notes: String? = null
)
