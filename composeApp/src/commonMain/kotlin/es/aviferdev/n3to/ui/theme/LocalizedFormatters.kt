package es.aviferdev.n3to.ui.theme

import androidx.compose.runtime.Composable
import es.aviferdev.n3to.platform.nowMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.date_days_ago
import n3to.composeapp.generated.resources.date_format_dmy
import n3to.composeapp.generated.resources.date_hours_ago
import n3to.composeapp.generated.resources.date_just_now
import n3to.composeapp.generated.resources.date_minutes_ago
import n3to.composeapp.generated.resources.date_months_ago
import n3to.composeapp.generated.resources.date_month_full_01
import n3to.composeapp.generated.resources.date_month_full_02
import n3to.composeapp.generated.resources.date_month_full_03
import n3to.composeapp.generated.resources.date_month_full_04
import n3to.composeapp.generated.resources.date_month_full_05
import n3to.composeapp.generated.resources.date_month_full_06
import n3to.composeapp.generated.resources.date_month_full_07
import n3to.composeapp.generated.resources.date_month_full_08
import n3to.composeapp.generated.resources.date_month_full_09
import n3to.composeapp.generated.resources.date_month_full_10
import n3to.composeapp.generated.resources.date_month_full_11
import n3to.composeapp.generated.resources.date_month_full_12
import n3to.composeapp.generated.resources.date_month_short_01
import n3to.composeapp.generated.resources.date_month_short_02
import n3to.composeapp.generated.resources.date_month_short_03
import n3to.composeapp.generated.resources.date_month_short_04
import n3to.composeapp.generated.resources.date_month_short_05
import n3to.composeapp.generated.resources.date_month_short_06
import n3to.composeapp.generated.resources.date_month_short_07
import n3to.composeapp.generated.resources.date_month_short_08
import n3to.composeapp.generated.resources.date_month_short_09
import n3to.composeapp.generated.resources.date_month_short_10
import n3to.composeapp.generated.resources.date_month_short_11
import n3to.composeapp.generated.resources.date_month_short_12
import n3to.composeapp.generated.resources.date_one_hour_ago
import n3to.composeapp.generated.resources.date_one_minute_ago
import n3to.composeapp.generated.resources.date_one_month_ago
import n3to.composeapp.generated.resources.date_one_year_ago
import n3to.composeapp.generated.resources.date_today
import n3to.composeapp.generated.resources.date_years_ago
import n3to.composeapp.generated.resources.date_yesterday
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
//  Nombres de meses localizados
// ─────────────────────────────────────────────────────────────────────────────

/** Devuelve los 12 nombres completos de mes desde recursos localizados. */
@Composable
fun localizedMonthNames(): List<String> = listOf(
    stringResource(Res.string.date_month_full_01),
    stringResource(Res.string.date_month_full_02),
    stringResource(Res.string.date_month_full_03),
    stringResource(Res.string.date_month_full_04),
    stringResource(Res.string.date_month_full_05),
    stringResource(Res.string.date_month_full_06),
    stringResource(Res.string.date_month_full_07),
    stringResource(Res.string.date_month_full_08),
    stringResource(Res.string.date_month_full_09),
    stringResource(Res.string.date_month_full_10),
    stringResource(Res.string.date_month_full_11),
    stringResource(Res.string.date_month_full_12),
)

/** Devuelve las 12 abreviaturas de mes (3 letras) desde recursos localizados. */
@Composable
fun localizedMonthShort(): List<String> = listOf(
    stringResource(Res.string.date_month_short_01),
    stringResource(Res.string.date_month_short_02),
    stringResource(Res.string.date_month_short_03),
    stringResource(Res.string.date_month_short_04),
    stringResource(Res.string.date_month_short_05),
    stringResource(Res.string.date_month_short_06),
    stringResource(Res.string.date_month_short_07),
    stringResource(Res.string.date_month_short_08),
    stringResource(Res.string.date_month_short_09),
    stringResource(Res.string.date_month_short_10),
    stringResource(Res.string.date_month_short_11),
    stringResource(Res.string.date_month_short_12),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Formateo de fechas localizado
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Versión localizada de [formatDate].
 * Devuelve "hoy", "ayer" o "14 may" según la distancia al día actual.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun formatDateLocalized(epochMillis: Long): String {
    val todayText = stringResource(Res.string.date_today)
    val yesterdayText = stringResource(Res.string.date_yesterday)
    val months = localizedMonthShort()

    val todayMillis = nowMillis()
    val todayDays = todayMillis / 86_400_000L
    val targetDays = epochMillis / 86_400_000L

    return when (todayDays - targetDays) {
        0L -> todayText
        1L -> yesterdayText
        else -> {
            val instant = Instant.fromEpochMilliseconds(epochMillis)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val month = months.getOrElse(local.monthNumber - 1) { "?" }
            "${local.dayOfMonth} $month"
        }
    }
}

/**
 * Versión localizada de [formatRelativeTime].
 * Devuelve cadenas como "ahora mismo", "hace 5 minutos", "hace 3 días"…
 */
@Composable
fun formatRelativeTimeLocalized(epochMillis: Long): String {
    val justNow   = stringResource(Res.string.date_just_now)
    val oneMinute = stringResource(Res.string.date_one_minute_ago)
    val minutes   = stringResource(Res.string.date_minutes_ago)
    val oneHour   = stringResource(Res.string.date_one_hour_ago)
    val hours     = stringResource(Res.string.date_hours_ago)
    val yesterday = stringResource(Res.string.date_yesterday)
    val days      = stringResource(Res.string.date_days_ago)
    val oneMonth  = stringResource(Res.string.date_one_month_ago)
    val months    = stringResource(Res.string.date_months_ago)
    val oneYear   = stringResource(Res.string.date_one_year_ago)
    val years     = stringResource(Res.string.date_years_ago)

    val now     = nowMillis()
    val diffMs  = (now - epochMillis).coerceAtLeast(0L)
    val seconds = diffMs / 1_000L
    val mins    = seconds / 60L
    val hrs     = mins / 60L
    val dys     = hrs   / 24L
    val mns     = dys   / 30L
    val yrs     = dys   / 365L

    return when {
        seconds < 45L         -> justNow
        mins    < 2L          -> oneMinute
        mins    < 60L         -> stringResource(Res.string.date_minutes_ago, mins)
        hrs     < 2L          -> oneHour
        hrs     < 24L         -> stringResource(Res.string.date_hours_ago, hrs)
        dys     < 2L          -> yesterday
        dys     < 30L         -> stringResource(Res.string.date_days_ago, dys)
        mns     < 2L          -> oneMonth
        dys     < 365L        -> stringResource(Res.string.date_months_ago, mns)
        yrs     < 2L          -> oneYear
        else                  -> stringResource(Res.string.date_years_ago, yrs)
    }
}

/**
 * Formatea una fecha como "14 de mayo de 2026" usando recursos localizados.
 * Especialmente útil para DatePickerRow y formularios.
 */
@Composable
fun formatDateFullLocalized(epochMillis: Long): String {
    val months = localizedMonthNames()
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = months.getOrElse(local.monthNumber - 1) { "?" }
    return stringResource(Res.string.date_format_dmy, local.dayOfMonth, monthName, local.year)
}
