package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.mergerequests.domain.DiffLine
import dev.tanuki.feature.mergerequests.domain.DiffLineType
import dev.tanuki.feature.mergerequests.domain.FileDiff
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MergeRequestDetailRoot(
    projectId: Long,
    iid: Long,
    onBack: () -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: MergeRequestDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(projectId, iid) { viewModel.load(projectId, iid) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MergeRequestDetailEvent.OpenInBrowser -> onOpenInBrowser(event.url)
        }
    }

    MergeRequestDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun MergeRequestDetailScreen(
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("‹ Back") }
            Spacer(Modifier.weight(1f))
            if (state.mergeRequest != null) {
                TextButton(onClick = { onAction(MergeRequestDetailAction.OnOpenInBrowser) }) {
                    Text("Open in GitLab")
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(MergeRequestDetailAction.OnRetry) }) { Text("Retry") }
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                state.mergeRequest?.let { mr ->
                    item { Header(mr, state.totalAdditions, state.totalDeletions, state.diffs.size) }
                }
                items(
                    count = state.diffs.size,
                    key = { index -> state.diffs[index].newPath + index },
                ) { index ->
                    FileDiffView(state.diffs[index])
                }
            }
        }
    }
}

@Composable
private fun Header(mr: MergeRequest, additions: Int, deletions: Int, fileCount: Int) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(mr.title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "${mr.reference} · ${mr.authorName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "${mr.sourceBranch} → ${mr.targetBranch}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = CodeFontFamily,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = buildString {
                append("+").append(additions).append(" −").append(deletions)
                append(" · ").append(fileCount).append(if (fileCount == 1) " file" else " files")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        mr.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun FileDiffView(file: FileDiff) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (file.isDeleted) file.oldPath else file.newPath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = CodeFontFamily,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "+${file.additions} −${file.deletions}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        file.lines.forEach { DiffLineRow(it) }
    }
}

@Composable
private fun DiffLineRow(line: DiffLine) {
    val colors = TanukiTheme.colors
    if (line.type == DiffLineType.HUNK) {
        Text(
            text = line.content,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 2.dp),
        )
        return
    }
    val background = when (line.type) {
        DiffLineType.ADDITION -> colors.diffAddedBackground
        DiffLineType.DELETION -> colors.diffRemovedBackground
        else -> MaterialTheme.colorScheme.surface
    }
    val sign = when (line.type) {
        DiffLineType.ADDITION -> "+"
        DiffLineType.DELETION -> "−"
        else -> " "
    }
    val signColor = when (line.type) {
        DiffLineType.ADDITION -> colors.diffAddedAccent
        DiffLineType.DELETION -> colors.diffRemovedAccent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 8.dp, vertical = 1.dp),
    ) {
        Text(
            text = sign,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = signColor,
            modifier = Modifier.width(14.dp),
        )
        Text(
            text = line.content,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
