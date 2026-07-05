package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import dev.tanuki.core.designsystem.DiffScrollbar
import dev.tanuki.core.designsystem.FileDiffView
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.domain.diff.DiffLine
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.Discussion
import dev.tanuki.feature.mergerequests.domain.DiscussionNote
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import dev.tanuki.feature.mergerequests.presentation.relativeTime
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

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
    var showReviewSheet by remember { mutableStateOf(false) }
    var showComposer by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val commenting = state.inSelectionMode && selectedTab == 3

    Box(modifier = Modifier.fillMaxSize()) {
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
                state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { onAction(MergeRequestDetailAction.OnRetry) }) { Text("Retry") }
                    }
                }
                state.mergeRequest != null -> {
                    val mr = state.mergeRequest
                    Header(mr, state.totalAdditions, state.totalDeletions, state.diffs.size)
                    MrTabs(
                        selected = selectedTab,
                        overviewCount = state.threadCount,
                        commitCount = state.commits.size,
                        pipelineCount = state.pipelines.size,
                        changeCount = state.diffs.size,
                        onSelect = { selectedTab = it },
                    )
                    if (commenting) {
                        SelectionBar(state.selectedCount) { onAction(MergeRequestDetailAction.OnCancelSelection) }
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (selectedTab) {
                            1 -> CommitsTab(state)
                            2 -> PipelinesTab(state)
                            3 -> ChangesTab(state, onAction)
                            else -> OverviewTab(mr, state, onAction)
                        }
                        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }

        if (state.mergeRequest != null && state.error == null) {
            ExtendedFloatingActionButton(
                onClick = { if (commenting) showComposer = true else showReviewSheet = true },
                icon = { Text(if (commenting) "💬" else "✓") },
                text = { Text(if (commenting) "Comment (${state.selectedCount})" else "Review") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (showReviewSheet && state.mergeRequest != null) {
        ReviewSheet(state = state, onAction = onAction, onDismiss = { showReviewSheet = false })
    }
    if (showComposer && state.inSelectionMode) {
        DiffCommentSheet(state = state, onAction = onAction, onDismiss = { showComposer = false })
    }
    state.activeThread?.let { thread -> ThreadSheet(thread = thread, state = state, onAction = onAction) }
}

@Composable
private fun MrTabs(
    selected: Int,
    overviewCount: Int,
    commitCount: Int,
    pipelineCount: Int,
    changeCount: Int,
    onSelect: (Int) -> Unit,
) {
    androidx.compose.material3.TabRow(selectedTabIndex = selected) {
        MrTab("Overview", overviewCount, selected == 0) { onSelect(0) }
        MrTab("Commits", commitCount, selected == 1) { onSelect(1) }
        MrTab("Pipelines", pipelineCount, selected == 2) { onSelect(2) }
        MrTab("Changes", changeCount, selected == 3) { onSelect(3) }
    }
}

@Composable
private fun MrTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = if (count > 0) "$label · $count" else label,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        },
    )
}

@Composable
private fun OverviewTab(
    mr: MergeRequest,
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
) {
    val threads = state.discussions.filter { it.notes.any { n -> !n.system } }
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        mr.description?.takeIf { it.isNotBlank() }?.let { desc ->
            item {
                Description(
                    markdown = desc,
                    projectBaseUrl = mr.webUrl.substringBefore("/-/"),
                    projectId = mr.projectId,
                    authToken = state.accessToken,
                )
            }
        }
        if (threads.isNotEmpty()) item { ActivityHeader() }
        items(count = threads.size, key = { "t-${threads[it].id}" }) { i ->
            DiscussionRow(threads[i]) { onAction(MergeRequestDetailAction.OnOpenThread(threads[i])) }
        }
        if (mr.description.isNullOrBlank() && threads.isEmpty()) {
            item { EmptyTab("Nothing here yet.") }
        }
    }
}

@Composable
private fun ChangesTab(state: MergeRequestDetailState, onAction: (MergeRequestDetailAction) -> Unit) {
    if (state.diffs.isEmpty()) {
        EmptyTab("No changes.")
        return
    }
    val listState = rememberLazyListState()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 96.dp)) {
            items(count = state.diffs.size, key = { index -> state.diffs[index].newPath + index }) { index ->
                val file = state.diffs[index]
                FileDiffView(
                    file = file,
                    onLineLongPress = { line ->
                        onAction(
                            MergeRequestDetailAction.OnStartSelection(
                                file.newPath, file.oldPath, line.newLine, line.oldLine,
                            ),
                        )
                    },
                    onLineClick = { line ->
                        if (state.inSelectionMode) {
                            onAction(
                                MergeRequestDetailAction.OnToggleLine(
                                    file.newPath, file.oldPath, line.newLine, line.oldLine,
                                ),
                            )
                        } else {
                            discussionForLine(state, file.newPath, file.oldPath, line)?.let {
                                onAction(MergeRequestDetailAction.OnOpenThread(it))
                            }
                        }
                    },
                    isLineSelected = { line -> lineSelected(state, file.newPath, line) },
                    lineHasComment = { line -> discussionForLine(state, file.newPath, file.oldPath, line) != null },
                )
            }
        }
        DiffScrollbar(listState, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun CommitsTab(state: MergeRequestDetailState) {
    if (state.commits.isEmpty()) {
        EmptyTab("No commits.")
        return
    }
    val now = remember { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(count = state.commits.size, key = { state.commits[it].shortId + it }) { i ->
            val c = state.commits[i]
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp),
            ) {
                Text(c.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(c.shortId, style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.primary)
                    Text(
                        (c.authorName?.let { "$it · " } ?: "") + relativeTime(c.createdAt, now),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelinesTab(state: MergeRequestDetailState) {
    if (state.pipelines.isEmpty()) {
        EmptyTab("No pipelines.")
        return
    }
    val now = remember { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(count = state.pipelines.size, key = { state.pipelines[it].id }) { i ->
            val p = state.pipelines[i]
            val color = when (p.status) {
                "success" -> TanukiTheme.colors.success
                "failed" -> MaterialTheme.colorScheme.error
                "running", "pending", "created" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
                Column(Modifier.weight(1f)) {
                    Text(p.ref, style = MaterialTheme.typography.bodyMedium, fontFamily = CodeFontFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "#${p.id} · ${p.status} · ${relativeTime(p.createdAt, now)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTab(text: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun discussionForLine(
    state: MergeRequestDetailState,
    newPath: String,
    oldPath: String,
    line: DiffLine,
): Discussion? =
    line.newLine?.let { state.commentByKey["$newPath#N$it"] }
        ?: line.oldLine?.let { state.commentByKey["$oldPath#O$it"] }

private fun lineSelected(state: MergeRequestDetailState, newPath: String, line: DiffLine): Boolean {
    val sel = state.selection ?: return false
    return sel.newPath == newPath && SelectedLine(line.newLine, line.oldLine) in sel.lines
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewSheet(
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
        ) {
            Text("Review", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.commentText,
                onValueChange = { onAction(MergeRequestDetailAction.OnCommentChange(it)) },
                label = { Text("Leave a comment / request changes") },
                enabled = !state.actionInProgress,
                minLines = 2,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Button(
                onClick = {
                    onAction(MergeRequestDetailAction.OnSendComment)
                    onDismiss()
                },
                enabled = state.commentText.isNotBlank() && !state.actionInProgress,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("Comment")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Button(
                onClick = {
                    onAction(MergeRequestDetailAction.OnApprove)
                    onDismiss()
                },
                enabled = !state.approved && !state.actionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = TanukiTheme.colors.success),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.approved) "Approved ✓" else "Approve")
            }
            Button(
                onClick = {
                    onAction(MergeRequestDetailAction.OnMerge)
                    onDismiss()
                },
                enabled = !state.actionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("Merge")
            }
        }
    }
}

/**
 * A lightweight scroll-position thumb on the right edge. Item-index based, so it's
 * approximate across wildly different file sizes, but gives a clear sense of position.
 */
@Composable
private fun SelectionBar(count: Int, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count line${if (count == 1) "" else "s"} selected · tap lines to add",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onCancel) {
            Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiffCommentSheet(
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sel = state.selection
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding(),
        ) {
            Text("New comment", style = MaterialTheme.typography.titleMedium)
            Text(
                text = (sel?.newPath?.substringAfterLast('/') ?: "line") +
                    " · line ${sel?.anchorNewLine ?: sel?.anchorOldLine} · ${state.selectedCount} selected",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = CodeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedTextField(
                value = state.diffCommentText,
                onValueChange = { onAction(MergeRequestDetailAction.OnDiffCommentChange(it)) },
                label = { Text("Your comment") },
                enabled = !state.isPostingDiffComment,
                minLines = 2,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Button(
                onClick = {
                    onAction(MergeRequestDetailAction.OnSubmitDiffComment)
                    onDismiss()
                },
                enabled = state.diffCommentText.isNotBlank() && !state.isPostingDiffComment,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("Comment on line")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadSheet(
    thread: Discussion,
    state: MergeRequestDetailState,
    onAction: (MergeRequestDetailAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onAction(MergeRequestDetailAction.OnCloseThread) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding(),
        ) {
            if (thread.isOnDiff) {
                Text(
                    text = "${thread.filePath?.substringAfterLast('/')} · line ${thread.newLine ?: thread.oldLine}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = CodeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Conversation", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (thread.resolvable) {
                    Text(
                        text = if (thread.resolved) "Resolved" else "Unresolved",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (thread.resolved) TanukiTheme.colors.success else MaterialTheme.colorScheme.error,
                    )
                }
            }

            thread.notes.filter { !it.system }.forEach { note -> NoteItem(note) }

            OutlinedTextField(
                value = state.threadReplyText,
                onValueChange = { onAction(MergeRequestDetailAction.OnThreadReplyChange(it)) },
                label = { Text("Reply…") },
                enabled = !state.isThreadBusy,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(MergeRequestDetailAction.OnSubmitReply) },
                    enabled = state.threadReplyText.isNotBlank() && !state.isThreadBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reply")
                }
                if (thread.resolvable) {
                    TextButton(
                        onClick = { onAction(MergeRequestDetailAction.OnResolveThread(!thread.resolved)) },
                        enabled = !state.isThreadBusy,
                    ) {
                        Text(if (thread.resolved) "Reopen" else "Resolve")
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteItem(note: DiscussionNote) {
    val now = remember { Clock.System.now() }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(note.authorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                relativeTime(note.createdAt, now),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = note.body,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ActivityHeader() {
    Text(
        text = "Activity",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun DiscussionRow(discussion: Discussion, onClick: () -> Unit) {
    val first = discussion.notes.firstOrNull { !it.system }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(first?.authorName ?: "Thread", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (discussion.isOnDiff) {
                    Text(
                        text = "${discussion.filePath?.substringAfterLast('/')}:${discussion.newLine ?: discussion.oldLine}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = first?.body?.replace("\n", " ") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (discussion.notes.count { !it.system } > 1) {
            Text(
                "${discussion.notes.count { !it.system }}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (discussion.resolvable && discussion.resolved) {
            Text("✓", color = TanukiTheme.colors.success)
        }
    }
}

@Composable
private fun Header(mr: MergeRequest, additions: Int, deletions: Int, fileCount: Int) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = mr.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(mr.status)
            Text(
                text = "${mr.reference} · ${mr.authorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${mr.sourceBranch} → ${mr.targetBranch}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = CodeFontFamily,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(modifier = Modifier.padding(top = 10.dp)) {
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
    }
}

@Composable
private fun Description(markdown: String, projectBaseUrl: String, projectId: Long, authToken: String?) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val blocks = remember(markdown, projectBaseUrl, projectId) {
        prepareDescription(markdown, projectBaseUrl, projectId)
    }

    // Headings one step smaller than the library defaults.
    val typography = markdownTypography(
        h1 = MaterialTheme.typography.headlineSmall,
        h2 = MaterialTheme.typography.titleLarge,
        h3 = MaterialTheme.typography.titleMedium,
        h4 = MaterialTheme.typography.titleSmall,
        h5 = MaterialTheme.typography.bodyLarge,
        h6 = MaterialTheme.typography.bodyMedium,
    )

    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (expanded) {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                blocks.forEach { block ->
                    when (block) {
                        is DescriptionBlock.Md -> Markdown(
                            content = block.markdown,
                            imageTransformer = Coil3ImageTransformerImpl,
                            typography = typography,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        is DescriptionBlock.Video -> InlineVideo(
                            url = block.url,
                            authToken = authToken,
                            aspectRatio = block.aspectRatio,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// Video embed with optional GitLab `{width=274 height=600}` sizing attrs.
private val VIDEO_EMBED = Regex(
    """!\[[^\]]*]\(([^)\s]+\.(?:mov|mp4|webm|m4v|avi))\)(?:\{([^}\n]*)\})?""",
    RegexOption.IGNORE_CASE,
)
private val IMAGE_UPLOAD = Regex("""(!\[[^\]]*]\()([^)\s]*/uploads/[^)\s]*)(\))""")
private val RELATIVE_URL = Regex("""]\((/[^)\s]*)\)""")
private val MEDIA_ATTRS = Regex("""(\))\{[^}\n]*\}""")
private val UPLOAD_PATH = Regex("""/uploads/([^/]+)/([^)\s?#]+)""")
private val HOST = Regex("""^https?://[^/]+""")
private val WIDTH = Regex("""width=(\d+)""")
private val HEIGHT = Regex("""height=(\d+)""")

private sealed interface DescriptionBlock {
    data class Md(val markdown: String) : DescriptionBlock
    data class Video(val url: String, val aspectRatio: Float?) : DescriptionBlock
}

/**
 * Split a GitLab description into ordered blocks — Markdown text and inline videos — so
 * they render in their original position. Upload URLs are rewritten to the authenticated
 * API form (`/api/v4/projects/:id/uploads/:secret/:file`); the web `/uploads/` route is
 * session-only, but the API route accepts our OAuth token.
 */
private fun prepareDescription(
    markdown: String,
    projectBaseUrl: String,
    projectId: Long,
): List<DescriptionBlock> {
    val host = HOST.find(projectBaseUrl)?.value ?: projectBaseUrl
    val blocks = mutableListOf<DescriptionBlock>()
    var last = 0
    for (m in VIDEO_EMBED.findAll(markdown)) {
        processText(markdown.substring(last, m.range.first), host, projectBaseUrl, projectId)
            ?.let { blocks += DescriptionBlock.Md(it) }
        val attrs = m.groupValues[2]
        val w = WIDTH.find(attrs)?.groupValues?.get(1)?.toFloatOrNull()
        val h = HEIGHT.find(attrs)?.groupValues?.get(1)?.toFloatOrNull()
        val aspect = if (w != null && h != null && h > 0f) w / h else null
        blocks += DescriptionBlock.Video(toApiUploadUrl(m.groupValues[1], host, projectId), aspect)
        last = m.range.last + 1
    }
    processText(markdown.substring(last), host, projectBaseUrl, projectId)
        ?.let { blocks += DescriptionBlock.Md(it) }
    return blocks
}

/** Rewrite image upload URLs to the API form, resolve other relative links, strip media attrs. */
private fun processText(text: String, host: String, projectBaseUrl: String, projectId: Long): String? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    var t = IMAGE_UPLOAD.replace(trimmed) { m ->
        "${m.groupValues[1]}${toApiUploadUrl(m.groupValues[2], host, projectId)}${m.groupValues[3]}"
    }
    t = RELATIVE_URL.replace(t) { m -> "](${projectBaseUrl}${m.groupValues[1]})" }
    t = MEDIA_ATTRS.replace(t) { m -> m.groupValues[1] }
    return t
}

/** Rewrite a `/uploads/:secret/:file` URL (relative or absolute) to the token-authenticated API path. */
private fun toApiUploadUrl(url: String, host: String, projectId: Long): String {
    val m = UPLOAD_PATH.find(url) ?: return url
    return "$host/api/v4/projects/$projectId/uploads/${m.groupValues[1]}/${m.groupValues[2]}"
}

@Composable
private fun StatusPill(status: MergeStatus) {
    val (label, color) = when (status) {
        MergeStatus.MERGEABLE -> "Mergeable" to TanukiTheme.colors.success
        MergeStatus.NEEDS_REBASE -> "Needs rebase" to TanukiTheme.colors.warning
        MergeStatus.DISCUSSIONS_UNRESOLVED -> "Unresolved threads" to MaterialTheme.colorScheme.error
        MergeStatus.CI_RUNNING -> "CI running" to MaterialTheme.colorScheme.secondary
        MergeStatus.DRAFT -> "Draft" to MaterialTheme.colorScheme.onSurfaceVariant
        MergeStatus.CONFLICTS -> "Conflicts" to MaterialTheme.colorScheme.error
        MergeStatus.BLOCKED -> "Blocked" to MaterialTheme.colorScheme.error
        MergeStatus.UNKNOWN -> "Open" to MaterialTheme.colorScheme.onSurfaceVariant
    }
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

