package es.aviferdev.n3to.domain.model

/**
 * Estado de un presupuesto anual para una categoría de gasto.
 *
 * @param categoryId   ID de la categoría.
 * @param categoryName Nombre visible de la categoría.
 * @param annualLimit  Límite configurado por el usuario (0 = sin límite).
 * @param limitType    Tipo de límite: fijo (€) o porcentaje de ingresos.
 * @param spent        Gasto acumulado en el año actual para esta categoría.
 * @param totalIncome  Ingresos totales del año (necesario para calcular %).
 * @param year         Año al que corresponden los datos (ej. "2026").
 */
data class CategoryBudgetStatus(
    val categoryId: String,
    val categoryName: String,
    val annualLimit: Double,
    val limitType: LimitType,
    val spent: Double,
    val totalIncome: Double,
    val year: String
) {

    /** Límite efectivo: si es FIXED → annualLimit; si es PERCENTAGE → totalIncome * annualLimit / 100 */
    val effectiveLimit: Double
        get() = when (limitType) {
            LimitType.FIXED -> annualLimit
            LimitType.PERCENTAGE -> totalIncome * annualLimit / 100.0
        }

    /** Progreso 0.0…1.0+ (puede superar 1.0 si se excede). */
    val progress: Float
        get() = if (effectiveLimit > 0.0) (spent / effectiveLimit).toFloat().coerceIn(0f, 2f) else 0f

    /** Cantidad restante hasta el límite (negativa si se ha excedido). */
    val remaining: Double
        get() = effectiveLimit - spent

    /** Ha superado el límite. */
    val isOverBudget: Boolean
        get() = spent > effectiveLimit && effectiveLimit > 0.0

    /** Está al 80% o más del límite (incluye over-budget). */
    val isNearLimit: Boolean
        get() = effectiveLimit > 0.0 && spent >= effectiveLimit * 0.8
}
