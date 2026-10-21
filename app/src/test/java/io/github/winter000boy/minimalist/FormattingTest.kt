package io.github.winter000boy.minimalist

import io.github.winter000boy.minimalist.ui.formatDate
import io.github.winter000boy.minimalist.ui.formatDuration
import io.github.winter000boy.minimalist.ui.formatTime
import io.github.winter000boy.minimalist.ui.ordinalSuffix
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FormattingTest {

    @Test
    fun formatsDateLikeTheDesign() {
        assertEquals("Tuesday, 20th October", formatDate(LocalDate.of(2026, 10, 20), Locale.UK))
    }

    @Test
    fun picksOrdinalSuffixes() {
        val expected = mapOf(1 to "st", 2 to "nd", 3 to "rd", 4 to "th", 11 to "th", 12 to "th", 13 to "th", 21 to "st", 22 to "nd", 23 to "rd", 31 to "st")
        expected.forEach { (day, suffix) -> assertEquals("day $day", suffix, ordinalSuffix(day)) }
    }

    @Test
    fun formatsTimeIn24And12Hour() {
        assertEquals("18:47", formatTime(LocalTime.of(18, 47), is24Hour = true))
        assertEquals("6:47", formatTime(LocalTime.of(18, 47), is24Hour = false))
        assertEquals("09:05", formatTime(LocalTime.of(9, 5), is24Hour = true))
    }

    @Test
    fun formatsScreenTime() {
        assertEquals("0m", formatDuration(Duration.ZERO))
        assertEquals("0m", formatDuration(59.seconds))
        assertEquals("45m", formatDuration(45.minutes))
        assertEquals("1h", formatDuration(60.minutes))
        assertEquals("1h 12m", formatDuration(72.minutes + 30.seconds))
        assertEquals("2h 5m", formatDuration(125.minutes))
        assertEquals("13h", formatDuration(13.hours))
    }
}
