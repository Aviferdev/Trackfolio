package es.aviferdev.n3to.domain.model

/**
 * Objetivo financiero mensual para una cuenta.
 *
 * @property accountId  Cuenta a la que pertenece el objetivo.
 * @property year       Año en formato "2026".
 * @property month      Mes en formato "01".."12".
 * @property savingsTarget    Objetivo de ahorro mensual (€): income - expense >= target.
 * @property investmentTarget Objetivo de inversión neta mensual (€): neto de compras - ventas de activos.
 */
data class MonthlyGoal(
    val accountId: String,
    val year: String,
    val month: String,
    val savingsTarget: Double,
    val investmentTarget: Double
)
