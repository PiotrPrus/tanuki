package dev.tanuki.feature.projects.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.presentation.components.ProjectRow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectsRoot(
    onOpenProject: (projectId: Long, projectName: String) -> Unit,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectsEvent.OpenDashboard -> onOpenProject(event.projectId, event.projectName)
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
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Text("Projects", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${state.visibleProjects.size} " +
                    if (state.visibleProjects.size == 1) "repository" else "repositories",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val tabs = ProjectFilter.entries
        PrimaryScrollableTabRow(
            selectedTabIndex = tabs.indexOf(state.filter).coerceAtLeast(0),
            edgePadding = 12.dp,
        ) {
            tabs.forEach { filter ->
                Tab(
                    selected = state.filter == filter,
                    onClick = { onAction(ProjectsAction.OnFilterChange(filter)) },
                    text = { Text(filter.label(), maxLines = 1, style = MaterialTheme.typography.labelLarge) },
                )
            }
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(ProjectsAction.OnQueryChange(it)) },
            label = { Text("Filter projects") },
            singleLine = true,
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onAction(ProjectsAction.OnQueryChange("")) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear filter")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            state.visibleProjects.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    if (state.query.isBlank()) "No projects here." else "No projects match \"${state.query}\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.visibleProjects, key = { it.id }) { project ->
                    ProjectRow(
                        project = project,
                        onOpen = { onAction(ProjectsAction.OnOpenProject(project)) },
                        onToggleStar = { onAction(ProjectsAction.OnToggleStar(project)) },
                    )
                }
            }
        }
    }
}

private fun ProjectFilter.label(): String = when (this) {
    ProjectFilter.RECENT -> "Recent"
    ProjectFilter.STARRED -> "Starred"
    ProjectFilter.PERSONAL -> "Personal"
    ProjectFilter.MEMBER -> "Member"
}
