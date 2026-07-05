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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme
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
            content != null -> FileBody(content, state.fileName)
        }
    }
}

@Composable
private fun FileBody(content: String, fileName: String) {
    val lines = remember(content) { content.split("\n") }
    val shown = remember(lines) { lines.take(MAX_LINES) }
    val numberWidth = remember(shown) { (shown.size.toString().length * 9 + 12).dp }

    val isKotlin = fileName.endsWith(".kt", true) || fileName.endsWith(".kts", true)
    // Highlight the whole (capped) file once, then slice per line so multi-line
    // constructs (block comments, strings) colour correctly.
    val highlighted: AnnotatedString? = remember(shown, isKotlin) {
        if (isKotlin) highlightKotlin(shown.joinToString("\n")) else null
    }
    val lineRanges = remember(shown) {
        var offset = 0
        shown.map { line -> (offset..offset + line.length).also { offset += line.length + 1 } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        shown.forEachIndexed { index, line ->
            val lineText: AnnotatedString = when {
                line.isEmpty() -> AnnotatedString(" ")
                highlighted != null -> highlighted.subSequence(lineRanges[index].first, lineRanges[index].last)
                else -> AnnotatedString(line)
            }
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
                    text = lineText,
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

// Approximates IntelliJ's default light scheme for the token categories a lexer can detect.
// (Per-symbol nuances — private/extension/parameter — need compiler resolution and aren't available.)
private val IntelliJLight = SyntaxTheme(
    key = "intellij-light",
    code = 0x080808,          // default text
    keyword = 0x0033B3,       // fun, val, private, by … (navy)
    string = 0x067D17,        // "strings" (green)
    literal = 0x1750EB,       // numbers, true/false (blue)
    comment = 0x8C8C8C,       // // line comments (grey)
    metadata = 0x9E880D,      // @annotations (gold)
    multilineComment = 0x8C8C8C,
    punctuation = 0x080808,
    mark = 0x871094,          // secondary keywords / soft tokens (purple)
)

/** Kotlin syntax highlighting via the Highlights engine → a styled AnnotatedString. */
private fun highlightKotlin(code: String): AnnotatedString {
    val spans = runCatching {
        Highlights.Builder()
            .code(code)
            .language(SyntaxLanguage.KOTLIN)
            .theme(IntelliJLight)
            .build()
            .getHighlights()
    }.getOrDefault(emptyList())

    return buildAnnotatedString {
        append(code)
        val len = code.length
        spans.forEach { h ->
            when (h) {
                is ColorHighlight -> {
                    val start = h.location.start.coerceIn(0, len)
                    val end = h.location.end.coerceIn(start, len)
                    if (end > start) addStyle(SpanStyle(color = Color(0xFF000000.toInt() or h.rgb)), start, end)
                }
                is BoldHighlight -> {
                    val start = h.location.start.coerceIn(0, len)
                    val end = h.location.end.coerceIn(start, len)
                    if (end > start) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                }
            }
        }
    }
}
