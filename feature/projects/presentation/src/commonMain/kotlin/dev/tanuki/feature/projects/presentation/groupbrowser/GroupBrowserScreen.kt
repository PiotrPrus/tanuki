package dev.tanuki.feature.projects.presentation.groupbrowser

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.presentation.components.GroupRow
import dev.tanuki.feature.projects.presentation.components.PathBreadcrumb
import dev.tanuki.feature.projects.presentation.components.ProjectRow
import dev.tanuki.feature.projects.presentation.components.SectionLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GroupBrowserRoot(
    groupFullPath: String,
    title: String,
    onBack: () -> Unit,
    onOpenGroup: (fullPath: String, name: String) -> Unit,
    onOpenProject: (projectId: Long, projectName: String) -> Unit,
    viewModel: GroupBrowserViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(groupFullPath) { viewModel.load(groupFullPath, title) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is GroupBrowserEvent.OpenGroup -> onOpenGroup(event.fullPath, event.name)
            is GroupBrowserEvent.OpenDashboard -> onOpenProject(event.projectId, event.projectName)
        }
    }

    GroupBrowserScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun GroupBrowserScreen(
    state: GroupBrowserState,
    onAction: (GroupBrowserAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) { Text("‹ Back") }

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
            PathBreadcrumb(
                pathWithNamespace = state.fullPath,
                onOpenGroup = { onAction(GroupBrowserAction.OnOpenGroupPath(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(GroupBrowserAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.subgroups.isEmpty() && state.projects.isEmpty() ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("This group is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.subgroups.isNotEmpty()) {
                    item(key = "subgroups-label") { SectionLabel("Subgroups") }
                    items(state.subgroups, key = { "g-${it.id}" }) { group ->
                        GroupRow(group = group, onOpen = { onAction(GroupBrowserAction.OnOpenSubgroup(group)) })
                    }
                }
                if (state.projects.isNotEmpty()) {
                    item(key = "projects-label") { SectionLabel("Projects") }
                    items(state.projects, key = { "p-${it.id}" }) { project ->
                        ProjectRow(
                            project = project,
                            onOpen = { onAction(GroupBrowserAction.OnOpenProject(project)) },
                            onToggleStar = { onAction(GroupBrowserAction.OnToggleStar(project)) },
                        )
                    }
                }
            }
        }
    }
}
