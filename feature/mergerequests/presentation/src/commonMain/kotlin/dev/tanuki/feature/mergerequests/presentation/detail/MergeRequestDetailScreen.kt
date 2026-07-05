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
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
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

