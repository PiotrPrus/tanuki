package dev.tanuki.feature.mergerequests.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestState
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import kotlin.time.Instant

/**
 * Shared merge-request card. [referenceText] lets callers show either a short
 * "!iid" (project-scoped list) or the full project path (cross-project list).
 */
@Composable
fun MergeRequestListItem(
    mr: MergeRequest,
    referenceText: String,
    now: Instant,
    onClick: () -> Unit,
) {
    val isOpen = mr.state == MergeRequestState.OPEN && !mr.isDraft
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(if (isOpen) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StateBadge(mr)
                    Text(
                        text = referenceText,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = relativeTime(mr.updatedAt, now),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Text(
                    text = mr.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOpen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    mr.status.indicator()?.let { (icon, label, tint) -> IconLabel(icon, label, tint) }
                    if (mr.commentCount > 0) {
                        IconLabel(
                            Icons.AutoMirrored.Filled.Comment,
                            mr.commentCount.toString(),
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Avatar(mr.authorAvatarUrl, mr.authorName)
        }
    }
}

@Composable
private fun StateBadge(mr: MergeRequest) {
    val (label, color) = when {
        mr.isDraft -> "Draft" to MaterialTheme.colorScheme.outline
        mr.state == MergeRequestState.MERGED -> "Merged" to MaterialTheme.colorScheme.secondary
        mr.state == MergeRequestState.CLOSED -> "Closed" to MaterialTheme.colorScheme.error
        else -> "Open" to MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun IconLabel(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Avatar(url: String?, name: String) {
    val shape = CircleShape
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(32.dp).clip(shape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        )
    } else {
        Box(
            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MergeStatus.indicator(): Triple<ImageVector, String, Color>? = when (this) {
    MergeStatus.MERGEABLE -> Triple(Icons.Filled.CheckCircle, "Ready", TanukiTheme.colors.success)
    MergeStatus.CI_RUNNING -> Triple(Icons.Filled.Autorenew, "CI running", MaterialTheme.colorScheme.primary)
    MergeStatus.CONFLICTS -> Triple(Icons.Filled.Warning, "Conflicts", MaterialTheme.colorScheme.error)
    MergeStatus.DISCUSSIONS_UNRESOLVED -> Triple(Icons.Filled.Forum, "Threads", MaterialTheme.colorScheme.tertiary)
    MergeStatus.NEEDS_REBASE -> Triple(Icons.Filled.Sync, "Needs rebase", MaterialTheme.colorScheme.tertiary)
    MergeStatus.BLOCKED -> Triple(Icons.Filled.Block, "Blocked", MaterialTheme.colorScheme.error)
    MergeStatus.DRAFT -> Triple(Icons.Filled.EditNote, "Draft", MaterialTheme.colorScheme.outline)
    MergeStatus.UNKNOWN -> null
}

/** Compact "2h ago" / "yesterday" / "3d ago" from [instant] relative to [now]. */
internal fun relativeTime(instant: Instant, now: Instant): String {
    val d = now - instant
    return when {
        d.inWholeMinutes < 1 -> "just now"
        d.inWholeHours < 1 -> "${d.inWholeMinutes}m ago"
        d.inWholeDays < 1 -> "${d.inWholeHours}h ago"
        d.inWholeDays < 2 -> "yesterday"
        d.inWholeDays < 7 -> "${d.inWholeDays}d ago"
        d.inWholeDays < 30 -> "${d.inWholeDays / 7}w ago"
        else -> "${d.inWholeDays / 30}mo ago"
    }
}
