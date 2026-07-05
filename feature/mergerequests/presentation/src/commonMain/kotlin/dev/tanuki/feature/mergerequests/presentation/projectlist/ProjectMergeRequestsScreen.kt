package dev.tanuki.feature.mergerequests.presentation.projectlist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.MergeRequestFilter
import dev.tanuki.feature.mergerequests.presentation.MergeRequestListItem
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

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
                        MergeRequestListItem(
                            mr = mr,
                            referenceText = "!${mr.iid}",
                            now = now,
                            onClick = { onAction(ProjectMergeRequestsAction.OnOpen(mr)) },
                        )
                    }
                }
            }
        }
    }
}

private fun MergeRequestFilter.label(): String = when (this) {
    MergeRequestFilter.OPENED -> "Open"
    MergeRequestFilter.MERGED -> "Merged"
    MergeRequestFilter.CLOSED -> "Closed"
    MergeRequestFilter.ALL -> "All"
}
