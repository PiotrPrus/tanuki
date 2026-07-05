package dev.tanuki.feature.projects.presentation.branches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.Branch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Composable
fun ProjectBranchesRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenMergeRequest: (projectId: Long, iid: Long) -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: ProjectBranchesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectBranchesEvent.OpenInBrowser -> onOpenInBrowser(event.url)
            is ProjectBranchesEvent.OpenMergeRequest -> onOpenMergeRequest(event.projectId, event.iid)
        }
    }
    ProjectBranchesScreen(
        state = state,
        projectName = projectName,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun ProjectBranchesScreen(
    state: ProjectBranchesState,
    projectName: String,
    onAction: (ProjectBranchesAction) -> Unit,
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
                text = "REPOSITORY",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = CodeFontFamily,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "Branches",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Development streams for $projectName.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectBranchesAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.branches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No branches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> BranchList(state.branches, onAction)
        }
    }
}

@Composable
private fun BranchList(
    branches: List<Branch>,
    onAction: (ProjectBranchesAction) -> Unit,
) {
    val now = remember(branches) { Clock.System.now() }
    // Stale = no commit in the last 90 days. Default branch always sorts first among active.
    val staleThreshold = 90.days
    val (stale, active) = remember(branches, now) {
        branches.partition { now - it.lastActivity > staleThreshold && !it.isDefault }
    }
    val activeSorted = remember(active) {
        active.sortedWith(compareByDescending<Branch> { it.isDefault }.thenByDescending { it.lastActivity })
    }
    val staleSorted = remember(stale) { stale.sortedByDescending { it.lastActivity } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (activeSorted.isNotEmpty()) {
            item(key = "h-active") { SectionHeader("Active", activeSorted.size, muted = false) }
            items(activeSorted, key = { "a-${it.name}" }) { branch ->
                BranchRow(branch, now, muted = false, onAction = onAction)
            }
        }
        if (staleSorted.isNotEmpty()) {
            item(key = "h-stale") { SectionHeader("Stale", staleSorted.size, muted = true) }
            items(staleSorted, key = { "s-${it.name}" }) { branch ->
                BranchRow(branch, now, muted = true, onAction = onAction)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, muted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .padding(horizontal = 8.dp, vertical = 1.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BranchRow(
    branch: Branch,
    now: Instant,
    muted: Boolean,
    onAction: (ProjectBranchesAction) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onAction(ProjectBranchesAction.OnOpenBranch(branch)) }
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val iconTint = when {
            muted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            branch.isDefault -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (muted) Icons.Filled.History else Icons.Filled.AltRoute,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = branch.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CodeFontFamily,
                    color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (branch.isProtected) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Protected",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (branch.isDefault) TinyChip("Default")
                Text(
                    text = branch.lastCommitAuthor
                        ?.let { "$it · ${relativeTime(branch.lastActivity, now)}" }
                        ?: "updated ${relativeTime(branch.lastActivity, now)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
        branch.openMergeRequestIid?.let { iid ->
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onAction(ProjectBranchesAction.OnOpenMergeRequest(iid)) }
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    Icons.Filled.CallMerge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    "!$iid",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun TinyChip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

/** Compact "5m ago" / "3d ago" / "4mo ago" from [instant] relative to [now]. */
private fun relativeTime(instant: Instant, now: Instant): String {
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
