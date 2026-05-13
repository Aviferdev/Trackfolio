package es.aviferdev.trackfolio.ui.theme

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val MONTH_SHORT = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

fun formatDate(epochMillis: Long): String {
    val todayMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val todayDays = todayMillis / 86_400_000L
    val targetDays = epochMillis / 86_400_000L

    return when (todayDays - targetDays) {
        0L -> "hoy"
        1L -> "ayer"
        else -> {
            val instant = Instant.fromEpochMilliseconds(epochMillis)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val month = MONTH_SHORT.getOrElse(local.monthNumber - 1) { "?" }
            "${local.dayOfMonth} $month"
        }
    }
}

fun formatAmount(amount: Double, addPositive: Boolean = false): String {
    val negative = amount < 0
    val abs = if (negative) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    val formatted = "$eurosStr,${cents.toString().padStart(2, '0')}"
    return if (negative) "-$formatted" else if (addPositive){
        "+$formatted"
    } else {
        formatted
    }
}

// ─── Moneda ──────────────────────────────────────────────────────────────────

/** Importe formateado en euros — "1.234,56 €". */
fun formatAmountEuro(amount: Double): String =
    "${formatAmount(amount)} €"

// ─── Tiempo relativo ─────────────────────────────────────────────────────────

/**
 * Devuelve una cadena legible del estilo «hace 5 minutos», «hace 3 días»,
 * «hace 2 meses» a partir de un epoch millis del pasado. Útil para mostrar
 * la frescura del precio actual de un activo.
 */
fun formatRelativeTime(epochMillis: Long): String {
    val now      = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val diffMs   = (now - epochMillis).coerceAtLeast(0L)
    val seconds  = diffMs / 1_000L
    val minutes  = seconds / 60L
    val hours    = minutes / 60L
    val days     = hours / 24L
    val months   = days / 30L
    val years    = days / 365L

    return when {
        seconds < 45L  -> "ahora mismo"
        minutes < 2L   -> "hace 1 minuto"
        minutes < 60L  -> "hace $minutes minutos"
        hours   < 2L   -> "hace 1 hora"
        hours   < 24L  -> "hace $hours horas"
        days    < 2L   -> "ayer"
        days    < 30L  -> "hace $days días"
        months  < 2L   -> "hace 1 mes"
        days    < 365L -> "hace $months meses"
        years   < 2L   -> "hace 1 año"
        else           -> "hace $years años"
    }
}

// ─── Formateo para axis de gráficos ──────────────────────────────────────────

/**
 * Formatea un valor para mostrarlo en el eje Y de un gráfico.
 * Usa abreviaturas K (miles) y M (millones) cuando corresponde.
 * Formato español: coma decimal, espacio como separador de miles.
 *
 * @param value valor numérico a formatear.
 * @return cadena formateada, ej: "1,2K €", "500 €", "2,5M €".
 */
fun formatAxisLabel(value: Double): String {
    val absVal = if (value < 0) -value else value
    val sign = if (value < 0) "-" else ""

    return when {
        absVal >= 1_000_000 -> {
            val m = value / 1_000_000
            "${sign}${formatCompact(m)}M €"
        }
        absVal >= 10_000 -> {
            val k = value / 1_000
            "${sign}${k.toLong()}K €"
        }
        absVal >= 1_000 -> {
            val k = value / 1_000
            "${sign}${formatCompact(k)}K €"
        }
        else -> {
            "${sign}${formatAmount(value)} €"
        }
    }
}

/**
 * Formatea un número con 1 decimal usando coma española.
 * Ej: 1.2 -> "1,2", 5.0 -> "5", 2.5 -> "2,5"
 */
private fun formatCompact(n: Double): String {
    val integer = n.toLong()
    val decimal = ((n - integer) * 10).toLong().coerceIn(0, 9)
    return if (decimal == 0L) "$integer" else "$integer,$decimal"
}
