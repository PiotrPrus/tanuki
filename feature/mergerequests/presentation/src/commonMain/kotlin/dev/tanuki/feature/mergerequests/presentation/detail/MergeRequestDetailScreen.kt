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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clipToBounds
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
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
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
    var showReviewSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 96.dp),
                        ) {
                            state.mergeRequest?.let { mr ->
                                item {
                                    Header(
                                        mr = mr,
                                        additions = state.totalAdditions,
                                        deletions = state.totalDeletions,
                                        fileCount = state.diffs.size,
                                        authToken = state.accessToken,
                                    )
                                }
                            }
                            items(
                                count = state.diffs.size,
                                key = { index -> state.diffs[index].newPath + index },
                            ) { index ->
                                FileDiffView(state.diffs[index])
                            }
                        }
                        DiffScrollbar(listState, Modifier.align(Alignment.CenterEnd))
                    }
                }
                SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
            }
        }

        if (state.mergeRequest != null && state.error == null) {
            ExtendedFloatingActionButton(
                onClick = { showReviewSheet = true },
                icon = { Text("✓") },
                text = { Text("Review") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (showReviewSheet && state.mergeRequest != null) {
        ReviewSheet(
            state = state,
            onAction = onAction,
            onDismiss = { showReviewSheet = false },
        )
    }
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
private fun DiffScrollbar(listState: LazyListState, modifier: Modifier = Modifier) {
    val visible by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            info.totalItemsCount > info.visibleItemsInfo.size && info.totalItemsCount > 0
        }
    }
    if (!visible) return

    val info = listState.layoutInfo
    val total = info.totalItemsCount
    val visibleCount = info.visibleItemsInfo.size.coerceAtLeast(1)
    val proportion = (visibleCount.toFloat() / total).coerceIn(0.06f, 1f)
    val progress = (listState.firstVisibleItemIndex.toFloat() / (total - visibleCount).coerceAtLeast(1))
        .coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier.fillMaxHeight().padding(vertical = 4.dp).width(4.dp),
    ) {
        val trackHeight = maxHeight
        val thumbHeight = trackHeight * proportion
        val offsetY = (trackHeight - thumbHeight) * progress
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .height(thumbHeight)
                .width(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
        )
    }
}

@Composable
private fun Header(mr: MergeRequest, additions: Int, deletions: Int, fileCount: Int, authToken: String?) {
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
        mr.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Description(
                markdown = desc,
                projectBaseUrl = mr.webUrl.substringBefore("/-/"),
                projectId = mr.projectId,
                authToken = authToken,
            )
        }
    }
}

@Composable
private fun Description(markdown: String, projectBaseUrl: String, projectId: Long, authToken: String?) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val prepared = remember(markdown, projectBaseUrl, projectId) {
        prepareMarkdown(markdown, projectBaseUrl, projectId)
    }
    val collapsible = prepared.markdown.length > 400

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .then(if (collapsible && !expanded) Modifier.heightIn(max = 260.dp) else Modifier)
            .clipToBounds(),
    ) {
        Markdown(
            content = prepared.markdown,
            imageTransformer = Coil3ImageTransformerImpl,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (collapsible) {
        TextButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(if (expanded) "Show less" else "Show more")
        }
    }
    prepared.videos.forEach { video ->
        InlineVideo(
            url = video.url,
            authToken = authToken,
            aspectRatio = video.aspectRatio,
            modifier = Modifier.padding(top = 12.dp),
        )
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

private data class VideoRef(val url: String, val aspectRatio: Float?)
private data class PreparedDescription(val markdown: String, val videos: List<VideoRef>)

/**
 * Prepare a GitLab description for rendering. Upload URLs are rewritten to the authenticated
 * API form (`/api/v4/projects/:id/uploads/:secret/:file`) — the web `/uploads/` route is
 * session-only, but the API route accepts our OAuth token. Video embeds are pulled out to
 * render as inline players, carrying any width/height so vertical clips get a portrait frame.
 */
private fun prepareMarkdown(
    markdown: String,
    projectBaseUrl: String,
    projectId: Long,
): PreparedDescription {
    val host = HOST.find(projectBaseUrl)?.value ?: projectBaseUrl
    val videos = mutableListOf<VideoRef>()

    var md = VIDEO_EMBED.replace(markdown) { m ->
        val attrs = m.groupValues[2]
        val w = WIDTH.find(attrs)?.groupValues?.get(1)?.toFloatOrNull()
        val h = HEIGHT.find(attrs)?.groupValues?.get(1)?.toFloatOrNull()
        val aspect = if (w != null && h != null && h > 0f) w / h else null
        videos += VideoRef(toApiUploadUrl(m.groupValues[1], host, projectId), aspect)
        ""
    }
    md = IMAGE_UPLOAD.replace(md) { m ->
        "${m.groupValues[1]}${toApiUploadUrl(m.groupValues[2], host, projectId)}${m.groupValues[3]}"
    }
    md = RELATIVE_URL.replace(md) { m -> "](${projectBaseUrl}${m.groupValues[1]})" }
    md = MEDIA_ATTRS.replace(md) { m -> m.groupValues[1] }
    return PreparedDescription(markdown = md.trim(), videos = videos)
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
