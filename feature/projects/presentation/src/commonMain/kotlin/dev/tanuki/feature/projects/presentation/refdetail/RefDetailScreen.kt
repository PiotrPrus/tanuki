package dev.tanuki.feature.projects.presentation.refdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.mikepenz.markdown.m3.Markdown
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.DiffScrollbar
import dev.tanuki.core.designsystem.FileDiffView
import dev.tanuki.core.designsystem.TanukiTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RefDetailRoot(
    projectId: Long,
    ref: String,
    fromRef: String?,
    title: String,
    onBack: () -> Unit,
    viewModel: RefDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId, ref) { viewModel.load(projectId, ref, fromRef, title) }
    RefDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun RefDetailScreen(
    state: RefDetailState,
    onAction: (RefDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("RELEASE", style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.secondary)
            Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            else -> {
                val listState = rememberLazyListState()
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Column(Modifier.padding(16.dp)) {
                                if (state.description != null) {
                                    Markdown(content = state.description)
                                } else {
                                    Text("No description.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            ChangesHeader(state)
                        }
                        itemsIndexed(state.diffs, key = { _, d -> d.newPath }) { _, diff ->
                            FileDiffView(diff, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                    DiffScrollbar(listState, Modifier.align(Alignment.CenterEnd))
                }
            }
        }
    }
}

@Composable
private fun ChangesHeader(state: RefDetailState) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when {
            !state.hasComparison -> Text(
                "First tag — nothing to compare against.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            state.diffs.isEmpty() -> Text(
                "No file changes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            else -> Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("${state.diffs.size} files", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("+${state.additions}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TanukiTheme.colors.diffAddedAccent)
                Text("−${state.deletions}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TanukiTheme.colors.diffRemovedAccent)
            }
        }
    }
}
