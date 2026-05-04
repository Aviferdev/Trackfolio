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

fun formatAmount(amount: Double): String {
    val abs = if (amount < 0) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}
