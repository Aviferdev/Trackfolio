package es.aviferdev.n3to.domain.model

/**
 * Objetivo financiero mensual para una cuenta.
 *
 * @property accountId  Cuenta a la que pertenece el objetivo.
 * @property year       Año (ej. 2026).
 * @property month      Mes (1..12 para overrides, 0 para el objetivo base del año).
 * @property savingsTarget    Objetivo de ahorro mensual (€): income - expense >= target.
 * @property investmentTarget Objetivo de inversión neta mensual (€): neto de compras - ventas de activos.
 */
data class MonthlyGoal(
    val accountId: String,
    val year: Int,
    val month: Int,
    val savingsTarget: Double,
    val investmentTarget: Double
)
