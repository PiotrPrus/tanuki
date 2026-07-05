package dev.tanuki.feature.mergerequests.presentation.projectlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestFilter
import dev.tanuki.feature.mergerequests.domain.MergeRequestState
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ProjectMergeRequestsRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenMergeRequest: (projectId: Long, iid: Long) -> Unit,
    viewModel: ProjectMergeRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectMergeRequestsEvent.OpenDetail -> onOpenMergeRequest(event.projectId, event.iid)
        }
    }
    ProjectMergeRequestsScreen(
        state = state,
        projectName = projectName,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun ProjectMergeRequestsScreen(
    state: ProjectMergeRequestsState,
    projectName: String,
    onAction: (ProjectMergeRequestsAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "MERGE REQUESTS",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = CodeFontFamily,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = projectName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MergeRequestFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onAction(ProjectMergeRequestsAction.OnSelectFilter(filter)) },
                    label = { Text(filter.label()) },
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectMergeRequestsAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.mergeRequests.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    "No ${state.filter.label().lowercase()} merge requests.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                val now = remember(state.mergeRequests) { Clock.System.now() }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.mergeRequests, key = { it.id }) { mr ->
                        MergeRequestCard(
                            mr = mr,
                            now = now,
                            onClick = { onAction(ProjectMergeRequestsAction.OnOpen(mr)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MergeRequestCard(mr: MergeRequest, now: Instant, onClick: () -> Unit) {
    val isOpen = mr.state == MergeRequestState.OPEN && !mr.isDraft
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp)),
    ) {
        // Left accent for actively-open MRs.
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
                        text = "!${mr.iid}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
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
                    mr.status.indicator()?.let { (icon, label, tint) ->
                        IconLabel(icon, label, tint)
                    }
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
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

private fun MergeRequestFilter.label(): String = when (this) {
    MergeRequestFilter.OPENED -> "Open"
    MergeRequestFilter.MERGED -> "Merged"
    MergeRequestFilter.CLOSED -> "Closed"
    MergeRequestFilter.ALL -> "All"
}

/** Compact "2h ago" / "yesterday" / "3d ago" from [instant] relative to [now]. */
private fun relativeTime(instant: Instant, now: Instant): String {
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
