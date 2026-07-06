package dev.tanuki.core.designsystem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tanuki.core.domain.diff.DiffLine
import dev.tanuki.core.domain.diff.DiffLineType
import dev.tanuki.core.domain.diff.FileDiff

/**
 * A collapsible file diff with red/green line highlighting. Reused across features.
 *
 * Optional per-line hooks power MR review interactions: [onLineClick]/[onLineLongPress] fire for
 * non-hunk lines, [isLineSelected] tints a line, and [lineHasComment] shows a thread indicator.
 */
@Composable
fun FileDiffView(
    file: FileDiff,
    modifier: Modifier = Modifier,
    onLineClick: ((DiffLine) -> Unit)? = null,
    onLineLongPress: ((DiffLine) -> Unit)? = null,
    isLineSelected: (DiffLine) -> Boolean = { false },
    lineHasComment: (DiffLine) -> Boolean = { false },
) {
    var expanded by rememberSaveable(file.newPath) { mutableStateOf(true) }
    val path = if (file.isDeleted) file.oldPath else file.newPath
    val name = path.substringAfterLast('/')
    val dir = path.substringBeforeLast('/', missingDelimiterValue = "")
    val shape = RoundedCornerShape(8.dp)

    // One inset, bordered container per file: header + (scrollable) diff body.
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 12.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // Body scrolls horizontally; each row fills at least the viewport width (so line
            // backgrounds span the container) but grows for long lines so nothing wraps.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val minRowWidth = maxWidth
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    file.lines.forEach { line ->
                        DiffLineRow(
                            line = line,
                            minWidth = minRowWidth,
                            selected = isLineSelected(line),
                            hasComment = lineHasComment(line),
                            onClick = onLineClick?.let { cb -> { cb(line) } },
                            onLongClick = onLineLongPress?.let { cb -> { cb(line) } },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiffLineRow(
    line: DiffLine,
    minWidth: Dp,
    selected: Boolean = false,
    hasComment: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = TanukiTheme.colors
    if (line.type == DiffLineType.HUNK) {
        Text(
            text = line.content,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            softWrap = false,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .widthIn(min = minWidth)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 10.dp, end = 12.dp, top = 2.dp, bottom = 2.dp),
        )
        return
    }
    val baseBackground = when (line.type) {
        DiffLineType.ADDITION -> colors.diffAddedBackground
        DiffLineType.DELETION -> colors.diffRemovedBackground
        else -> MaterialTheme.colorScheme.surface
    }
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else baseBackground
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
    val interactive = onClick != null || onLongClick != null
    Row(
        modifier = Modifier
            .widthIn(min = minWidth)
            .then(
                if (interactive) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick,
                    )
                } else {
                    Modifier
                },
            )
            .background(background)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left gutter accent marks lines that have a comment thread (visible at scroll origin).
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(if (hasComment) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent),
        )
        Text(
            text = sign,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = signColor,
            modifier = Modifier.padding(start = 6.dp).width(12.dp),
        )
        Text(
            text = line.content,
            fontFamily = CodeFontFamily,
            fontSize = 12.sp,
            softWrap = false,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 12.dp),
        )
    }
}

/** Thin overlay scrollbar for a diff [LazyListState]. */
@Composable
fun DiffScrollbar(listState: LazyListState, modifier: Modifier = Modifier) {
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
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .offset(y = offsetY)
                .height(thumbHeight)
                .width(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
        )
    }
}
