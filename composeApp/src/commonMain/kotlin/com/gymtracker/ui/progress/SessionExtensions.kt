package com.gymtracker.ui.progress

import com.gymtracker.Sessions
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

fun Sessions.metricValue(metric: ProgressMetric): Double = when (metric) {
    ProgressMetric.MAX_WEIGHT -> weightKg
    ProgressMetric.VOLUME     -> sets * reps * weightKg
    ProgressMetric.ONE_RM     -> if (reps > 0) weightKg * (1 + reps / 30.0) else weightKg
}

fun List<Sessions>.toDayScores(metric: ProgressMetric): List<DayScore> {
    val tz = TimeZone.currentSystemDefault()
    return groupBy { session ->
        Instant.fromEpochMilliseconds(session.date)
            .toLocalDateTime(tz)
            .date
    }.map { (localDate, daySessions) ->
        val value = when (metric) {
            ProgressMetric.VOLUME     -> daySessions.sumOf { it.sets * it.reps * it.weightKg }
            ProgressMetric.MAX_WEIGHT -> daySessions.maxOf { it.weightKg }
            ProgressMetric.ONE_RM     -> daySessions.maxOf { it.metricValue(ProgressMetric.ONE_RM) }
        }
        val epochMs = localDate.atStartOfDayIn(tz).toEpochMilliseconds()
        DayScore(epochMs, value)
    }.sortedBy { it.date }
}

fun formatDayLabel(epochMs: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}"
}
