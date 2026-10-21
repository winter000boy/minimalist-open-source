package io.github.winter000boy.minimalist

import io.github.winter000boy.minimalist.data.ActivityEvent
import io.github.winter000boy.minimalist.data.ActivityEvent.Kind.Paused
import io.github.winter000boy.minimalist.data.ActivityEvent.Kind.Resumed
import io.github.winter000boy.minimalist.data.ActivityEvent.Kind.ScreenOff
import io.github.winter000boy.minimalist.data.ActivityEvent.Kind.Stopped
import io.github.winter000boy.minimalist.data.foregroundMillis
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTimeTest {

    private fun total(vararg events: ActivityEvent, start: Long = 0, end: Long = 1_000) =
        foregroundMillis(events.asSequence(), start, end)

    @Test
    fun addsUpSeparateSessions() {
        assertEquals(
            40,
            total(
                ActivityEvent(10, Resumed, "a/.Main"),
                ActivityEvent(40, Paused, "a/.Main"),
                ActivityEvent(50, Resumed, "b/.Main"),
                ActivityEvent(60, Paused, "b/.Main"),
            ),
        )
    }

    @Test
    fun stopAfterPauseDoesNotCountAgain() {
        assertEquals(
            30,
            total(
                ActivityEvent(10, Resumed, "a/.Main"),
                ActivityEvent(40, Paused, "a/.Main"),
                ActivityEvent(41, Stopped, "a/.Main"),
            ),
        )
    }

    @Test
    fun stopWithoutPauseEndsTheSession() {
        assertEquals(20, total(ActivityEvent(10, Resumed, "a/.Main"), ActivityEvent(30, Stopped, "a/.Main")))
    }

    @Test
    fun movingBetweenActivitiesOfOneAppIsOneSession() {
        assertEquals(
            40,
            total(
                ActivityEvent(10, Resumed, "a/.List"),
                ActivityEvent(20, Paused, "a/.List"),
                ActivityEvent(20, Resumed, "a/.Detail"),
                ActivityEvent(25, Stopped, "a/.List"),
                ActivityEvent(50, Paused, "a/.Detail"),
            ),
        )
    }

    @Test
    fun splitScreenCountsOnce() {
        assertEquals(
            40,
            total(
                ActivityEvent(10, Resumed, "a/.Main"),
                ActivityEvent(20, Resumed, "b/.Main"),
                ActivityEvent(40, Paused, "a/.Main"),
                ActivityEvent(50, Paused, "b/.Main"),
            ),
        )
    }

    @Test
    fun repeatedResumeKeepsTheFirst() {
        assertEquals(
            20,
            total(
                ActivityEvent(10, Resumed, "a/.Main"),
                ActivityEvent(20, Resumed, "a/.Main"),
                ActivityEvent(30, Paused, "a/.Main"),
            ),
        )
    }

    @Test
    fun appInFrontAtMidnightCountsFromMidnight() {
        assertEquals(30, total(ActivityEvent(130, Paused, "a/.Main"), start = 100))
    }

    @Test
    fun appStoppedBeforeMidnightDoesNotCount() {
        // Its pause happened yesterday, so it wasn't in front at the start.
        assertEquals(0, total(ActivityEvent(130, Stopped, "a/.Main"), start = 100))
    }

    @Test
    fun appStillInFrontCountsUntilNow() {
        assertEquals(10, total(ActivityEvent(90, Resumed, "a/.Main"), end = 100))
    }

    @Test
    fun screenOffEndsEverything() {
        assertEquals(
            40,
            total(
                ActivityEvent(10, Resumed, "a/.Main"),
                ActivityEvent(20, Resumed, "b/.Main"),
                ActivityEvent(40, ScreenOff),
                ActivityEvent(45, Paused, "a/.Main"),
                ActivityEvent(46, Paused, "b/.Main"),
                // Never got its pause before a shutdown: ends there, not at the end of the window.
                ActivityEvent(60, Resumed, "c/.Main"),
                ActivityEvent(70, ScreenOff),
            ),
        )
    }

    @Test
    fun unmatchedPauseAfterScreenOffIsNotCarriedOver() {
        assertEquals(0, total(ActivityEvent(10, ScreenOff), ActivityEvent(50, Paused, "a/.Main")))
    }

    @Test
    fun staysInsideTheWindow() {
        assertEquals(
            20,
            total(
                ActivityEvent(80, Resumed, "a/.Main"),
                ActivityEvent(110, Paused, "a/.Main"),
                ActivityEvent(190, Resumed, "a/.Main"),
                start = 100,
                end = 200,
            ),
        )
    }

    @Test
    fun nothingHappenedIsZero() {
        assertEquals(0, total())
    }
}
