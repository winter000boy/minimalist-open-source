package io.github.winter000boy.minimalist.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** What the home screen knows about today's screen time. */
sealed interface ScreenTime {
    /** Not read yet. */
    data object Unknown : ScreenTime

    /** The user hasn't turned on usage access for us. */
    data object NoAccess : ScreenTime

    /** Time spent in other apps since local midnight. */
    data class Today(val time: Duration) : ScreenTime
}

/** Reads today's screen time from UsageStatsManager. Needs the "Usage access" special permission. */
class ScreenTimeRepository(private val context: Context) {

    /** Does disk work: call off the main thread. */
    fun today(): ScreenTime {
        if (!hasUsageAccess()) return ScreenTime.NoAccess
        val now = ZonedDateTime.now()
        val end = now.toInstant().toEpochMilli()
        val start = now.toLocalDate().atStartOfDay(now.zone).toInstant().toEpochMilli()
        return ScreenTime.Today(foregroundMillis(events(start, end), start, end).milliseconds)
    }

    private fun hasUsageAccess(): Boolean {
        val mode = context.getSystemService(AppOpsManager::class.java)
            .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        // "Default" defers to the permission itself, which only system apps are granted.
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Lifecycle events of other apps' activities. Time on our own home screen isn't screen time. */
    private fun events(start: Long, end: Long): Sequence<ActivityEvent> = sequence {
        val usageEvents = context.getSystemService(UsageStatsManager::class.java).queryEvents(start, end)
        val event = UsageEvents.Event()
        while (usageEvents.getNextEvent(event)) {
            if (event.packageName == context.packageName) continue
            val kind = event.kind() ?: continue
            yield(ActivityEvent(event.timeStamp, kind, "${event.packageName}/${event.className}"))
        }
    }
}

// The newer event types are plain ints; older Androids just never report them.
@SuppressLint("InlinedApi")
private fun UsageEvents.Event.kind(): ActivityEvent.Kind? = when (eventType) {
    // Same values as the MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND that Android 8 and 9 report.
    UsageEvents.Event.ACTIVITY_RESUMED -> ActivityEvent.Kind.Resumed
    UsageEvents.Event.ACTIVITY_PAUSED -> ActivityEvent.Kind.Paused
    UsageEvents.Event.ACTIVITY_STOPPED -> ActivityEvent.Kind.Stopped
    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
    UsageEvents.Event.DEVICE_SHUTDOWN,
    UsageEvents.Event.DEVICE_STARTUP,
    -> ActivityEvent.Kind.ScreenOff
    else -> null
}

/** One activity lifecycle event, reduced to what [foregroundMillis] needs. */
data class ActivityEvent(
    val time: Long,
    val kind: Kind,
    /** Anything unique to the activity, e.g. its component name. */
    val activity: String = "",
) {
    enum class Kind {
        Resumed,
        Paused,
        Stopped,

        /** The screen went off, or the phone shut down or booted: nothing is in front any more. */
        ScreenOff,
    }
}

/**
 * Milliseconds between [start] and [end] during which at least one activity was in front,
 * from [events] in time order. Activities side by side in split screen count once.
 */
fun foregroundMillis(events: Sequence<ActivityEvent>, start: Long, end: Long): Long {
    val resumedAt = HashMap<String, Long>()
    val seen = HashSet<String>()
    // An activity whose first event is a pause was already in front at [start]. That stops
    // being possible once the screen has gone off, since everything gets paused then.
    var mayCarryOver = true
    val spans = ArrayList<Pair<Long, Long>>()

    for (event in events) {
        when (event.kind) {
            ActivityEvent.Kind.Resumed -> resumedAt.getOrPut(event.activity) { event.time }
            ActivityEvent.Kind.Paused, ActivityEvent.Kind.Stopped -> {
                val carriedOver = mayCarryOver && event.kind == ActivityEvent.Kind.Paused && event.activity !in seen
                val from = resumedAt.remove(event.activity) ?: start.takeIf { carriedOver }
                if (from != null) spans += from to event.time
            }
            ActivityEvent.Kind.ScreenOff -> {
                resumedAt.values.forEach { spans += it to event.time }
                resumedAt.clear()
                mayCarryOver = false
            }
        }
        seen += event.activity
    }
    // Still in front right now.
    resumedAt.values.forEach { spans += it to end }

    return coveredMillis(spans, start, end)
}

/** Total length of [spans] within [start]..[end], counting overlaps once. */
private fun coveredMillis(spans: List<Pair<Long, Long>>, start: Long, end: Long): Long {
    var total = 0L
    var coveredUntil = start
    for ((from, to) in spans.sortedBy { it.first }) {
        val newFrom = maxOf(from, coveredUntil)
        val newTo = minOf(to, end)
        if (newTo > newFrom) {
            total += newTo - newFrom
            coveredUntil = newTo
        }
    }
    return total
}
