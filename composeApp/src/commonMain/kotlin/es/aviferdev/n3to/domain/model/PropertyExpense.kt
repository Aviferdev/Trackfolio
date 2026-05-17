package es.aviferdev.n3to.domain.model

/**
 * Representa un gasto asociado a la compra o venta de una propiedad.
 * Cada gasto tiene una categoría específica para identificar su naturaleza
 * (notaría, ITP/IVA, comisión inmobiliaria, plusvalía municipal, etc.).
 */
data class PropertyExpense(
    val categoryId: String,
    val amount: Double,
    val notes: String? = null
)
