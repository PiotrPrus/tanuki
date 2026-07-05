package dev.tanuki.feature.projects.presentation.code

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import org.koin.compose.viewmodel.koinViewModel

private const val MAX_LINES = 3000

@Composable
fun FileViewRoot(
    projectId: Long,
    ref: String,
    filePath: String,
    fileName: String,
    onBack: () -> Unit,
    viewModel: FileViewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(filePath) { viewModel.load(projectId, ref, filePath, fileName) }
    FileViewScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun FileViewScreen(
    state: FileViewState,
    onAction: (FileViewAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Text(
            text = state.fileName,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = CodeFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val content = state.content
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(FileViewAction.OnRetry) }) { Text("Retry") }
                }
            }
            content != null && content.any { it.code == 0 } -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Binary file — can't preview.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content != null -> FileBody(content)
        }
    }
}

@Composable
private fun FileBody(content: String) {
    val lines = remember(content) { content.split("\n") }
    val shown = remember(lines) { lines.take(MAX_LINES) }
    val numberWidth = remember(shown) { (shown.size.toString().length * 9 + 12).dp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        shown.forEachIndexed { index, line ->
            Row {
                Text(
                    text = "${index + 1}",
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(numberWidth).padding(end = 12.dp),
                )
                Text(
                    text = line.ifEmpty { " " },
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    softWrap = false,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        }
        if (lines.size > MAX_LINES) {
            Text(
                text = "… ${lines.size - MAX_LINES} more lines. Open in GitLab to see the full file.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
