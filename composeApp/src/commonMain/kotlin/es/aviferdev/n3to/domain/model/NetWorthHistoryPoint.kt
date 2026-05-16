package es.aviferdev.n3to.domain.model

/**
 * Punto de evolución del patrimonio neto mensual.
 * Se usa para construir el gráfico de evolución del patrimonio.
 *
 * @property yearMonth cadena "YYYY-MM" identificando el mes.
 * @property netWorth patrimonio neto en ese mes (activos − pasivos).
 * @property totalAssets total de activos (cuentas + portfolio + renta fija).
 * @property totalLiabilities total de pasivos (préstamos + deudas).
 */
data class NetWorthHistoryPoint(
    val yearMonth: String,
    val netWorth: Double,
    val totalAssets: Double,
    val totalLiabilities: Double
)
