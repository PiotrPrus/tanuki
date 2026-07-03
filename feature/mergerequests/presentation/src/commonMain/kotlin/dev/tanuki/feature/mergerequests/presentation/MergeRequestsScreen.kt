package dev.tanuki.feature.mergerequests.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MergeRequestsRoot(
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: MergeRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MergeRequestsEvent.OpenInBrowser -> onOpenInBrowser(event.url)
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
            text = "Review requested",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.mergeRequests, key = { it.id }) { mr ->
                    MergeRequestCard(mr = mr, onClick = { onAction(MergeRequestsAction.OnOpen(mr)) })
                }
            }
        }
    }
}

@Composable
private fun MergeRequestCard(mr: MergeRequest, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(mr.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                text = "${mr.reference} · ${mr.authorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusChip(mr.status)
                if (mr.commentCount > 0) {
                    Text(
                        text = "💬 ${mr.commentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: MergeStatus) {
    val (label, color) = status.presentation()
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun MergeStatus.presentation(): Pair<String, Color> = when (this) {
    MergeStatus.MERGEABLE -> "Mergeable" to Color(0xFF1A7F37)
    MergeStatus.NEEDS_REBASE -> "Needs rebase" to Color(0xFFB08800)
    MergeStatus.DISCUSSIONS_UNRESOLVED -> "Unresolved threads" to Color(0xFFCF222E)
    MergeStatus.CI_RUNNING -> "CI running" to Color(0xFF0969DA)
    MergeStatus.DRAFT -> "Draft" to Color(0xFF6E7781)
    MergeStatus.CONFLICTS -> "Conflicts" to Color(0xFFCF222E)
    MergeStatus.BLOCKED -> "Blocked" to Color(0xFFCF222E)
    MergeStatus.UNKNOWN -> "Open" to Color(0xFF6E7781)
}
