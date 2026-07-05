package dev.tanuki.feature.projects.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.projects.domain.PipelineStatus
import dev.tanuki.feature.projects.domain.ProjectDetail
import dev.tanuki.feature.projects.domain.ProjectStats
import dev.tanuki.feature.projects.domain.Visibility
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectDashboardRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenMergeRequests: (projectId: Long, projectName: String) -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: ProjectDashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ObserveDashboardEvents(viewModel, onOpenInBrowser)
    ProjectDashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenMergeRequests = { onOpenMergeRequests(projectId, projectName) },
    )
}

@Composable
private fun ObserveDashboardEvents(
    viewModel: ProjectDashboardViewModel,
    onOpenInBrowser: (String) -> Unit,
) {
    dev.tanuki.core.presentation.ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectDashboardEvent.OpenInBrowser -> onOpenInBrowser(event.url)
        }
    }
}

@Composable
fun ProjectDashboardScreen(
    state: ProjectDashboardState,
    onAction: (ProjectDashboardAction) -> Unit,
    onBack: () -> Unit,
    onOpenMergeRequests: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("‹ Back") }
            Spacer(Modifier.weight(1f))
            if (state.detail != null) {
                TextButton(onClick = { onAction(ProjectDashboardAction.OnOpenInBrowser) }) {
                    Text("Open in GitLab")
                }
            }
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectDashboardAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.detail != null -> DashboardContent(state.detail, state.stats, onOpenMergeRequests)
        }
    }
}

private enum class Tone { PRIMARY, SUCCESS, ERROR, MUTED }

private data class BentoTile(
    val label: String,
    val icon: ImageVector,
    val subtitle: String,
    val featured: Boolean = false,
    val tone: Tone = Tone.MUTED,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun DashboardContent(
    detail: ProjectDetail,
    stats: ProjectStats?,
    onOpenMergeRequests: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        // Breadcrumb + visibility
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisibilityPill(detail.visibility)
            Text(
                text = "/ ${detail.pathWithNamespace.replace("/", " / ")}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = CodeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Text(
            text = detail.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp),
        )
        detail.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCount(Icons.Filled.Star, detail.starCount.toString())
            StatCount(Icons.Filled.AltRoute, detail.forksCount.toString())
            stats?.contributors?.let { StatCount(Icons.Filled.Group, it.toString()) }
        }

        val codeSubtitle = listOfNotNull(detail.defaultBranch, formatRepoSize(detail.repositorySizeBytes))
            .joinToString(" • ").ifBlank { "Repository" }
        val (pipelineLabel, pipelineTone) = pipelineDisplay(stats?.latestPipeline)
        val tiles = listOf(
            BentoTile(
                "Merge Requests",
                Icons.Filled.CallMerge,
                stats?.openMergeRequests?.let { "$it Open" } ?: "Open",
                featured = true,
                tone = Tone.PRIMARY,
                onClick = onOpenMergeRequests,
            ),
            BentoTile("Code", Icons.Filled.Folder, codeSubtitle),
            BentoTile("Tags", Icons.Filled.Label, stats?.latestTag ?: stats?.tags?.let { "$it total" } ?: "Latest"),
            BentoTile("Releases", Icons.Filled.RocketLaunch, stats?.releases?.let { "$it total" } ?: "View"),
            BentoTile("Pipelines", Icons.Filled.CheckCircle, pipelineLabel, tone = pipelineTone),
            BentoTile("Branches", Icons.Filled.AltRoute, stats?.branches?.let { "$it Active" } ?: "View"),
        )
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { tile -> BentoCard(tile, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        ActivityPulse(activity = stats?.commitActivity.orEmpty(), modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun StatCount(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(value, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun BentoCard(tile: BentoTile, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(16.dp),
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .then(if (tile.onClick != null) Modifier.clickable(onClick = tile.onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        val chipColor = if (tile.featured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        val iconTint = if (tile.featured) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        Box(
            modifier = Modifier.size(48.dp).background(chipColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tile.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(tile.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = tile.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = when (tile.tone) {
                    Tone.PRIMARY -> MaterialTheme.colorScheme.primary
                    Tone.SUCCESS -> TanukiTheme.colors.success
                    Tone.ERROR -> MaterialTheme.colorScheme.error
                    Tone.MUTED -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ActivityPulse(activity: List<Int>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        val total = activity.sum()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Activity Pulse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                when {
                    // 100 is our per-page cap — the real window count may be higher.
                    total >= 100 -> "100+ commits · ${activity.size}d"
                    total > 0 -> "$total commits · ${activity.size}d"
                    else -> "Last ${activity.size.coerceAtLeast(14)} days"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            activity.isEmpty() -> ActivityBaseline()
            total == 0 -> Text(
                "No commits in the last ${activity.size} days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
            )
            else -> {
                val max = activity.max()
                Row(
                    modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    activity.forEach { count ->
                        val fraction = (count.toFloat() / max).coerceAtLeast(if (count > 0) 0.08f else 0.02f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f + fraction * 0.4f),
                                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                ),
                        )
                    }
                }
            }
        }
    }
}

/** Flat placeholder shown while commit activity is still loading. */
@Composable
private fun ActivityBaseline() {
    Row(
        modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(14) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.05f)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                    ),
            )
        }
    }
}

private fun pipelineDisplay(status: PipelineStatus?): Pair<String, Tone> = when (status) {
    PipelineStatus.SUCCESS -> "Passed" to Tone.SUCCESS
    PipelineStatus.FAILED -> "Failed" to Tone.ERROR
    PipelineStatus.RUNNING -> "Running" to Tone.PRIMARY
    PipelineStatus.OTHER -> "See status" to Tone.MUTED
    null -> "View" to Tone.MUTED
}

/** Bytes → a compact "2.4 MB" style string without platform String.format. */
private fun formatRepoSize(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    val mb = bytes / (1024.0 * 1024.0)
    return when {
        mb >= 1024 -> "${((mb / 1024) * 10).toLong() / 10.0} GB"
        mb >= 1 -> "${(mb * 10).toLong() / 10.0} MB"
        else -> "${bytes / 1024} KB"
    }
}

@Composable
private fun VisibilityPill(visibility: Visibility) {
    val (label, color) = when (visibility) {
        Visibility.PUBLIC -> "PUBLIC" to MaterialTheme.colorScheme.primary
        Visibility.INTERNAL -> "INTERNAL" to MaterialTheme.colorScheme.secondary
        Visibility.PRIVATE -> "PRIVATE" to MaterialTheme.colorScheme.onSurfaceVariant
        Visibility.UNKNOWN -> return
    }
    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = CodeFontFamily,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}
