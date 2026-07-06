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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.presentation.components.GroupRow
import dev.tanuki.feature.projects.presentation.components.ProjectRow
import dev.tanuki.feature.projects.presentation.components.SectionLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectsRoot(
    onOpenProject: (projectId: Long, projectName: String) -> Unit,
    onOpenGroup: (fullPath: String, name: String) -> Unit,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectsEvent.OpenDashboard -> onOpenProject(event.projectId, event.projectName)
            is ProjectsEvent.OpenGroup -> onOpenGroup(event.fullPath, event.name)
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
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            Text("Projects", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (state.searchMode) {
                    "${state.searchResults.size} " + if (state.searchResults.size == 1) "result" else "results"
                } else {
                    "${state.groups.size} groups · ${state.personalProjects.size} personal"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(ProjectsAction.OnQueryChange(it)) },
            label = { Text("Search all projects") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            state.searchMode -> SearchResults(state, onAction)
            else -> Hierarchy(state, onAction)
        }
    }
}

@Composable
private fun SearchResults(state: ProjectsState, onAction: (ProjectsAction) -> Unit) {
    when {
        state.isSearching && state.searchResults.isEmpty() ->
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.searchResults.isEmpty() ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No projects match \"${state.query}\".", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.searchResults, key = { it.id }) { project ->
                ProjectRow(
                    project = project,
                    onOpen = { onAction(ProjectsAction.OnOpenProject(project)) },
                    onToggleStar = { onAction(ProjectsAction.OnToggleStar(project)) },
                )
            }
        }
    }
}

@Composable
private fun Hierarchy(state: ProjectsState, onAction: (ProjectsAction) -> Unit) {
    if (state.groups.isEmpty() && state.personalProjects.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Nothing here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.groups.isNotEmpty()) {
            item(key = "groups-label") { SectionLabel("Groups") }
            items(state.groups, key = { "g-${it.id}" }) { group ->
                GroupRow(group = group, onOpen = { onAction(ProjectsAction.OnOpenGroup(group)) })
            }
        }
        if (state.personalProjects.isNotEmpty()) {
            item(key = "personal-label") { SectionLabel("Personal projects") }
            items(state.personalProjects, key = { "p-${it.id}" }) { project ->
                ProjectRow(
                    project = project,
                    onOpen = { onAction(ProjectsAction.OnOpenProject(project)) },
                    onToggleStar = { onAction(ProjectsAction.OnToggleStar(project)) },
                )
            }
        }
    }
}
