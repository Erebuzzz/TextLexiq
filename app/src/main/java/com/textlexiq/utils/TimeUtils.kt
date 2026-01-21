package com.textlexiq.utils

import java.time.Clock
import java.time.Duration
import java.time.Instant

fun formatRelativeTime(timestampMillis: Long, clock: Clock = Clock.systemDefaultZone()): String {
    val now = Instant.now(clock)
    val instant = Instant.ofEpochMilli(timestampMillis)
    val duration = Duration.between(instant, now)

    return when {
        duration.isNegative -> "Just now"
        duration.seconds < 60 -> "Just now"
        duration.toMinutes() < 60 -> "Updated ${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "Updated ${duration.toHours()}h ago"
        duration.toDays() < 7 -> "Updated ${duration.toDays()}d ago"
        else -> "Updated ${duration.toDays() / 7}w ago"
    }
}
