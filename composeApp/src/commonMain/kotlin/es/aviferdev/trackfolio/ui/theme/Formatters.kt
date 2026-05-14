package es.aviferdev.trackfolio.ui.theme

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Nombres completos de meses (español). */
val MONTH_NAMES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

/** Abreviaturas de 3 letras para meses. */
val MONTH_SHORT = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

/** Etiquetas ultra-cortas de 1 letra para gráficos de 12 columnas. */
val MONTH_LABELS = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

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

/**
 * Formatea una fecha como "14/5/2026" (día/mes/año sin padding).
 * Útil para vencimientos y fechas absolutas donde no aplica el formato relativo.
 */
fun formatDateShort(epochMillis: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis)
    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
    val dateTime = instant.toLocalDateTime(tz)
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
}

// ─── Porcentaje ───────────────────────────────────────────────────────────────

/**
 * Formatea un número como porcentaje con 1 decimal (formato español).
 * Ej: 25.5 → "25,5", 3.0 → "3,0"
 */
fun formatPercent(value: Double): String {
    val absVal = if (value < 0) -value else value
    val intPart = absVal.toLong()
    val decPart = ((absVal - intPart) * 10 + 0.5).toInt()
    val sign = if (value < 0) "-" else ""
    return "$sign$intPart,$decPart"
}

/**
 * Formatea un número como porcentaje con 1 decimal (formato español con coma),
 * añadiendo signo explícito para valores positivos.
 * Ej: 25.5 → "+25,5%", -5.0 → "-5,0%"
 */
fun formatPercentSigned(value: Double): String {
    val sign = if (value >= 0) "+" else ""
    return "$sign${formatPercent(value)}%"
}

// ─── Cantidades (unidades / participaciones) ──────────────────────────────────

/**
 * Formatea una cantidad de unidades/participaciones con redondeo a 2 decimales.
 * Ej: 14.6667 → "14,67", 5.0 → "5".
 *
 * @param value Cantidad a formatear.
 * @param decimals Número de decimales (por defecto 2; usar 3 para criptomonedas).
 */
fun formatQty(value: Double, decimals: Int = 2): String {
    if (value == value.toLong().toDouble()) return value.toLong().toString()
    val multiplier = when (decimals) {
        2 -> 100L; 3 -> 1000L; 4 -> 10000L; 6 -> 1_000_000L
        else -> 100L
    }
    val rounded = (value * multiplier + 0.5).toLong()
    val intPart = rounded / multiplier
    val decPart = rounded % multiplier
    val decStr = decPart.toString().padStart(decimals, '0').trimEnd('0').ifEmpty { "0" }
    return "$intPart,$decStr"
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
