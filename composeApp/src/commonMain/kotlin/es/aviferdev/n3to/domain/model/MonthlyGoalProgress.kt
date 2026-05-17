package es.aviferdev.n3to.domain.model

/**
 * Progreso mensual del objetivo, combina la meta (target) con el valor real.
 *
 * @property year               Año.
 * @property month              Mes.
 * @property savingsTarget      Objetivo de ahorro (€).
 * @property savingsActual      Ahorro real del mes (income - expense).
 * @property savingsAchieved    true si savingsActual >= savingsTarget (y target > 0).
 * @property savingsProgress    Progreso 0.0..1.0 (clamped).
 * @property investmentTarget   Objetivo de inversión neta (€).
 * @property investmentActual   Inversión neta real del mes (BUY - SELL).
 * @property investmentAchieved true si investmentActual >= investmentTarget (y target > 0).
 * @property investmentProgress Progreso 0.0..1.0 (clamped).
 */
data class MonthlyGoalProgress(
    val year: String,
    val month: String,
    val savingsTarget: Double,
    val savingsActual: Double,
    val savingsAchieved: Boolean,
    val savingsProgress: Float,
    val investmentTarget: Double,
    val investmentActual: Double,
    val investmentAchieved: Boolean,
    val investmentProgress: Float
) {
    /** true si al menos uno de los dos objetivos está definido (> 0). */
    val hasAnyGoal: Boolean get() = savingsTarget > 0.0 || investmentTarget > 0.0

    companion object {
        /**
         * Construye un [MonthlyGoalProgress] a partir de los datos crudos
         * y calcula automáticamente los flags y progresos.
         */
        fun from(
            year: String,
            month: String,
            goal: MonthlyGoal?,
            savingsActual: Double,
            investmentActual: Double
        ): MonthlyGoalProgress {
            val savingsTarget = goal?.savingsTarget ?: 0.0
            val investmentTarget = goal?.investmentTarget ?: 0.0

            return MonthlyGoalProgress(
                year = year,
                month = month,
                savingsTarget = savingsTarget,
                savingsActual = savingsActual,
                savingsAchieved = savingsTarget > 0.0 && savingsActual >= savingsTarget,
                savingsProgress = calculateProgress(savingsActual, savingsTarget),
                investmentTarget = investmentTarget,
                investmentActual = investmentActual,
                investmentAchieved = investmentTarget > 0.0 && investmentActual >= investmentTarget,
                investmentProgress = calculateProgress(investmentActual, investmentTarget)
            )
        }

        private fun calculateProgress(actual: Double, target: Double): Float =
            if (target <= 0.0) 1.0f else (actual / target).toFloat().coerceIn(0f, 1f)
    }
}
