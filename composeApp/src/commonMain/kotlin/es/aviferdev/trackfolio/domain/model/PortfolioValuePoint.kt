package es.aviferdev.trackfolio.domain.model

/**
 * Punto de valor del portfolio en el tiempo.
 * Se usa para construir el gráfico de evolución del valor de inversiones.
 *
 * @property date epoch millis del primer día del mes representado.
 * @property value valor total del portfolio (inversiones + renta fija) en esa fecha.
 */
data class PortfolioValuePoint(
    val date: Long,
    val value: Double
)
