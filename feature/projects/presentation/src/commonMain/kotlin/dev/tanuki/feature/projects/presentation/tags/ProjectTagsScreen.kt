package dev.tanuki.feature.projects.presentation.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.feature.projects.domain.Tag
import dev.tanuki.feature.projects.presentation.common.relativeTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun ProjectTagsRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenTag: (ref: String, fromRef: String?, title: String) -> Unit,
    viewModel: ProjectTagsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ProjectTagsScreen(state = state, projectName = projectName, onAction = viewModel::onAction, onBack = onBack, onOpenTag = onOpenTag)
}

@Composable
fun ProjectTagsScreen(
    state: ProjectTagsState,
    projectName: String,
    onAction: (ProjectTagsAction) -> Unit,
    onBack: () -> Unit,
    onOpenTag: (ref: String, fromRef: String?, title: String) -> Unit = { _, _, _ -> },
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("REPOSITORY", style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.secondary)
            Text("Tags", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Release points for $projectName.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectTagsAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.tags.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No tags yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val now = remember(state.tags) { Clock.System.now() }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(state.tags, key = { _, t -> t.name }) { index, tag ->
                        TagRow(
                            tag = tag,
                            updated = relativeTime(tag.lastActivity, now),
                            onClick = { onOpenTag(tag.name, state.tags.getOrNull(index + 1)?.name, tag.name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagRow(tag: Tag, updated: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Sell, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CodeFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (tag.hasRelease) TinyChip("Release")
                if (tag.isProtected) {
                    Icon(Icons.Filled.Lock, contentDescription = "Protected", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
            val subtitle = tag.message ?: tag.commitTitle
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = "$updated" + (tag.commitAuthor?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun TinyChip(label: String) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 1.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}
