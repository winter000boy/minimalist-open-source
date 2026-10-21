package io.github.winter000boy.minimalist.ui

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration

/** "18:47", or "6:47" on a 12-hour phone. */
fun formatTime(time: LocalTime, is24Hour: Boolean): String =
    time.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm"))

/** "Tuesday, 20th October" in English; the locale's own long form elsewhere. */
fun formatDate(date: LocalDate, locale: Locale): String {
    if (locale.language != Locale.ENGLISH.language) {
        val pattern = DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM")
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
    val day = date.dayOfMonth
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val month = date.month.getDisplayName(TextStyle.FULL, locale)
    return "$weekday, $day${ordinalSuffix(day)} $month"
}

/** "1h 12m", "45m", or "2h" on the hour. */
fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    return when {
        hours == 0L -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

internal fun ordinalSuffix(day: Int): String = when {
    day % 100 in 11..13 -> "th"
    day % 10 == 1 -> "st"
    day % 10 == 2 -> "nd"
    day % 10 == 3 -> "rd"
    else -> "th"
}
