package dev.tanuki.feature.projects.presentation.common

import kotlin.time.Instant

/** Compact "5m ago" / "3d ago" / "4mo ago" from [instant] relative to [now]. */
internal fun relativeTime(instant: Instant, now: Instant): String {
    val d = now - instant
    return when {
        d.inWholeMinutes < 1 -> "just now"
        d.inWholeHours < 1 -> "${d.inWholeMinutes}m ago"
        d.inWholeDays < 1 -> "${d.inWholeHours}h ago"
        d.inWholeDays < 7 -> "${d.inWholeDays}d ago"
        d.inWholeDays < 30 -> "${d.inWholeDays / 7}w ago"
        d.inWholeDays < 365 -> "${d.inWholeDays / 30}mo ago"
        else -> "${d.inWholeDays / 365}y ago"
    }
}
