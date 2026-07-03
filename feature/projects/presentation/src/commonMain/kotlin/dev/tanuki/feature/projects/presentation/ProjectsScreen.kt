package dev.tanuki.feature.projects.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.Visibility
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectsRoot(
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectsEvent.OpenInBrowser -> onOpenInBrowser(event.url)
        }
    }

    ProjectsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ProjectsScreen(
    state: ProjectsState,
    onAction: (ProjectsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Projects",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(ProjectsAction.OnQueryChange(it)) },
            label = { Text("Search projects") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProjectFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onAction(ProjectsAction.OnFilterChange(filter)) },
                    label = { Text(filter.label()) },
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            state.visibleProjects.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No projects here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.visibleProjects, key = { it.id }) { project ->
                    ProjectCard(project = project, onClick = { onAction(ProjectsAction.OnOpen(project)) })
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                VisibilityBadge(project.visibility)
            }
            Text(
                text = project.pathWithNamespace,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            project.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "★ ${project.starCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                project.lastActivity?.take(10)?.let { date ->
                    Text(
                        text = "Updated $date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun VisibilityBadge(visibility: Visibility) {
    val (label, color) = when (visibility) {
        Visibility.PUBLIC -> "Public" to TanukiTheme.colors.success
        Visibility.INTERNAL -> "Internal" to MaterialTheme.colorScheme.secondary
        Visibility.PRIVATE -> "Private" to MaterialTheme.colorScheme.onSurfaceVariant
        Visibility.UNKNOWN -> return
    }
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(percent = 50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

private fun ProjectFilter.label(): String = when (this) {
    ProjectFilter.ALL -> "All"
    ProjectFilter.STARRED -> "Starred"
    ProjectFilter.PERSONAL -> "Personal"
    ProjectFilter.SHARED -> "Shared"
}
