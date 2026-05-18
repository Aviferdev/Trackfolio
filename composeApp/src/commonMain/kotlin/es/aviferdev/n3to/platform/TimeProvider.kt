package es.aviferdev.n3to.platform

import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun nowMillis(): Long

@OptIn(ExperimentalTime::class)
fun nowLocalDateTime(): LocalDateTime =
    Instant.fromEpochMilliseconds(nowMillis())
        .toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(ExperimentalTime::class)
fun nowLocalDate(): LocalDate = nowLocalDateTime().date

@OptIn(ExperimentalTime::class)
fun nowYear(): Int = nowLocalDateTime().year

@OptIn(ExperimentalTime::class)
fun nowMonth(): Int = nowLocalDateTime().monthNumber

@OptIn(ExperimentalTime::class)
fun nowHour(): Int = nowLocalDateTime().hour
