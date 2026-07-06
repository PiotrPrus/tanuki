package dev.tanuki.feature.projects.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.projects.domain.Group
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.Visibility

/** A repository row with a star/unstar toggle — used in the projects list and group browser. */
@Composable
fun ProjectRow(project: Project, onOpen: () -> Unit, onToggleStar: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val namespace = project.pathWithNamespace.substringBeforeLast('/', missingDelimiterValue = "")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onOpen)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialAvatar(project.name)
        Column(modifier = Modifier.weight(1f)) {
            if (namespace.isNotEmpty()) {
                Text(
                    text = namespace.replace("/", " / ") + " /",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            project.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Row(modifier = Modifier.padding(top = 6.dp)) { VisibilityPill(project.visibility) }
        }
        StarToggle(starred = project.starred, count = project.starCount, onToggle = onToggleStar)
    }
}

/** A group/subgroup row — tapping drills into it. */
@Composable
fun GroupRow(group: Group, onOpen: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onOpen)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialAvatar(group.name)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            group.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Clickable star with count. Filled + gold when starred, outline otherwise. */
@Composable
fun StarToggle(starred: Boolean, count: Int, onToggle: () -> Unit) {
    val tint = if (starred) TanukiTheme.colors.warning else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (starred) Icons.Filled.Star else Icons.Filled.StarBorder,
            contentDescription = if (starred) "Unstar" else "Star",
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
    }
}

/**
 * Clickable path breadcrumb. Every segment except the last (the current project/group) is a group
 * whose cumulative path is passed to [onOpenGroup].
 */
@Composable
fun PathBreadcrumb(
    pathWithNamespace: String,
    onOpenGroup: (fullPath: String) -> Unit,
    modifier: Modifier = Modifier,
    lastIsGroup: Boolean = false,
) {
    val segments = pathWithNamespace.split("/").filter { it.isNotBlank() }
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        segments.forEachIndexed { i, seg ->
            val isLast = i == segments.lastIndex
            val clickable = !isLast || lastIsGroup
            val cumulative = segments.take(i + 1).joinToString("/")
            if (i > 0) {
                Text(
                    "  /  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = seg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (clickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = if (clickable) {
                    Modifier.clip(RoundedCornerShape(4.dp)).clickable { onOpenGroup(cumulative) }.padding(horizontal = 2.dp)
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
fun InitialAvatar(name: String) {
    val accents = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    val accent = accents[(name.hashCode().rem(accents.size) + accents.size) % accents.size]
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
fun VisibilityPill(visibility: Visibility) {
    val (label, color) = when (visibility) {
        Visibility.PUBLIC -> "Public" to TanukiTheme.colors.success
        Visibility.INTERNAL -> "Internal" to MaterialTheme.colorScheme.secondary
        Visibility.PRIVATE -> "Private" to MaterialTheme.colorScheme.onSurfaceVariant
        Visibility.UNKNOWN -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = color)
    }
}

fun formatCount(n: Int): String =
    if (n >= 1000) "${(n / 100) / 10.0}k" else n.toString()

/** Section label used above Groups / Projects lists. */
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}
