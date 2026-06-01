package es.aviferdev.n3to.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/**
 * Helpers para convertir año/mes a rangos de epoch millis (timezone UTC).
 *
 * Todas las fechas en la BD se almacenan como epoch millis. Estos helpers
 * convierten representaciones de año/mes a pares [start, end) para usar
 * en queries SQL con filtro por intervalo.
 */
object DateRangeHelper {

    /**
     * Calcula el rango [inicio, fin) en epoch millis para un año completo.
     *
     * @param year Año como string (ej. "2024")
     * @return Par (startEpochMillis, endEpochMillis)
     */
    fun yearEpochRange(year: String): Pair<Long, Long> {
        val y = year.toInt()
        val start = LocalDate(y, 1, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val end = LocalDate(y + 1, 1, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        return start to end
    }

    /**
     * Calcula el rango [inicio, fin) en epoch millis para un mes específico.
     *
     * @param year  Año como string (ej. "2024")
     * @param month Mes como string (ej. "3" para marzo)
     * @return Par (startEpochMillis, endEpochMillis)
     */
    fun monthEpochRange(year: String, month: String): Pair<Long, Long> {
        val y = year.toInt()
        val m = month.toInt()
        val start = LocalDate(y, m, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val end = if (m == 12)
            LocalDate(y + 1, 1, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        else
            LocalDate(y, m + 1, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        return start to end
    }

    /**
     * Calcula el rango [inicio, fin) en epoch millis para un mes específico
     * usando el formato "yyyy-MM".
     *
     * @param yearMonth Año y mes en formato "yyyy-MM" (ej. "2024-03")
     * @return Par (startEpochMillis, endEpochMillis)
     */
    fun monthEpochRange(yearMonth: String): Pair<Long, Long> {
        val parts = yearMonth.split("-")
        return monthEpochRange(parts[0], parts[1])
    }
}
