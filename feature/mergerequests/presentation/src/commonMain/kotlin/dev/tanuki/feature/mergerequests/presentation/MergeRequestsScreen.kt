package dev.tanuki.feature.mergerequests.presentation

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun MergeRequestsRoot(
    onOpenMergeRequest: (projectId: Long, iid: Long) -> Unit,
    viewModel: MergeRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MergeRequestsEvent.OpenDetail -> onOpenMergeRequest(event.projectId, event.iid)
        }
    }

    MergeRequestsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun MergeRequestsScreen(
    state: MergeRequestsState,
    onAction: (MergeRequestsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReviewScope.entries.forEach { scope ->
                FilterChip(
                    selected = state.scope == scope,
                    onClick = { onAction(MergeRequestsAction.OnSelectScope(scope)) },
                    label = { Text(scope.label) },
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(MergeRequestsAction.OnRefresh) }) { Text("Retry") }
                }
            }
            state.mergeRequests.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = when (state.scope) {
                        ReviewScope.ASSIGNED_TO_ME -> "Nothing assigned to you."
                        ReviewScope.REVIEW_REQUESTED -> "No reviews requested."
                    },
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
                            referenceText = mr.reference,
                            now = now,
                            onClick = { onAction(MergeRequestsAction.OnOpen(mr)) },
                        )
                    }
                }
            }
        }
    }
}
