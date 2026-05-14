package es.aviferdev.trackfolio.ui.common.chart

/**
 * Rangos temporales para filtrar el gráfico de evolución.
 *
 * @property label Etiqueta visible en el chip (ej: "1M").
 * @property windowDays Ventana en días hacia atrás desde hoy para filtrar puntos.
 *          45 días en 1M asegura capturar 2 cierres mensuales incluso a principio de mes.
 */
enum class TimeRange(val label: String, val windowDays: Int) {
    LAST_MONTH("1M", 45),
    LAST_YEAR("1A", 365),
    ALL_TIME("Todo", Int.MAX_VALUE)
}
