package dev.tanuki.feature.projects.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.ProjectDetail
import dev.tanuki.feature.projects.domain.Visibility
import org.koin.compose.viewmodel.koinViewModel

private data class DashboardTile(val label: String, val icon: String, val subtitle: String)

@Composable
fun ProjectDashboardRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: ProjectDashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectDashboardEvent.OpenInBrowser -> onOpenInBrowser(event.url)
        }
    }
    ProjectDashboardScreen(state = state, projectName = projectName, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun ProjectDashboardScreen(
    state: ProjectDashboardState,
    projectName: String,
    onAction: (ProjectDashboardAction) -> Unit,
    onBack: () -> Unit,
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
            state.detail != null -> DashboardContent(state.detail)
            else -> Unit
        }
    }
}

@Composable
private fun DashboardContent(detail: ProjectDetail) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text(detail.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = detail.pathWithNamespace,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VisibilityPill(detail.visibility)
            Text(
                text = "★ ${detail.starCount}   ⑂ ${detail.forksCount}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        detail.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        val tiles = listOf(
            DashboardTile("Merge requests", "🔀", "Open"),
            DashboardTile("Code", "📄", detail.defaultBranch ?: "Repository"),
            DashboardTile("Branches", "🌿", "View"),
            DashboardTile("Tags", "🏷", "View"),
            DashboardTile("Releases", "🚀", "View"),
            DashboardTile("Pipelines", "✅", "View"),
        )
        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tile -> StatTile(tile, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTile(tile: DashboardTile, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(92.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Text(tile.icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text(tile.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = tile.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VisibilityPill(visibility: Visibility) {
    val (label, color) = when (visibility) {
        Visibility.PUBLIC -> "Public" to TanukiTheme.colors.success
        Visibility.INTERNAL -> "Internal" to MaterialTheme.colorScheme.secondary
        Visibility.PRIVATE -> "Private" to MaterialTheme.colorScheme.onSurfaceVariant
        Visibility.UNKNOWN -> "" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    if (label.isEmpty()) return
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(percent = 50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
