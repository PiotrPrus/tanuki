package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.DiffLine
import dev.tanuki.feature.mergerequests.domain.DiffLineType
import dev.tanuki.feature.mergerequests.domain.FileDiff
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import kotlinx.coroutines.launch
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(projectId, iid) { viewModel.load(projectId, iid) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MergeRequestDetailEvent.OpenInBrowser -> onOpenInBrowser(event.url)
            is MergeRequestDetailEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    }

    MergeRequestDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun MergeRequestDetailScreen(
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
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

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
            SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }

        if (state.mergeRequest != null && state.error == null) {
            ActionBar(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun ActionBar(
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.commentText,
                    onValueChange = { onAction(MergeRequestDetailAction.OnCommentChange(it)) },
                    placeholder = { Text("Add a comment") },
                    singleLine = true,
                    enabled = !state.actionInProgress,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onAction(MergeRequestDetailAction.OnSendComment) },
                    enabled = state.commentText.isNotBlank() && !state.actionInProgress,
                ) {
                    Text("Send")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onAction(MergeRequestDetailAction.OnApprove) },
                    enabled = !state.approved && !state.actionInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = TanukiTheme.colors.success),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.approved) "Approved ✓" else "Approve")
                }
                Button(
                    onClick = { onAction(MergeRequestDetailAction.OnMerge) },
                    enabled = !state.actionInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Merge")
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
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "+$additions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TanukiTheme.colors.diffAddedAccent,
            )
            Text(
                text = " −$deletions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TanukiTheme.colors.diffRemovedAccent,
            )
            Text(
                text = " · $fileCount " + if (fileCount == 1) "file" else "files",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
    var expanded by rememberSaveable(file.newPath) { mutableStateOf(true) }
    val path = if (file.isDeleted) file.oldPath else file.newPath
    val name = path.substringAfterLast('/')
    val dir = path.substringBeforeLast('/', missingDelimiterValue = "")

    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "▾" else "▸",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(18.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = CodeFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (dir.isNotEmpty()) {
                    Text(
                        text = dir,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "+${file.additions}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TanukiTheme.colors.diffAddedAccent,
            )
            Text(
                text = " −${file.deletions}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TanukiTheme.colors.diffRemovedAccent,
            )
        }
        if (expanded) {
            file.lines.forEach { DiffLineRow(it) }
        }
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
