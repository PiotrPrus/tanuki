package dev.tanuki.feature.projects.presentation.pipelines

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
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
import dev.tanuki.feature.projects.domain.Pipeline
import dev.tanuki.feature.projects.presentation.common.relativeTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun ProjectPipelinesRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenPipeline: (pipelineId: Long, ref: String) -> Unit,
    viewModel: ProjectPipelinesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ProjectPipelinesScreen(state = state, projectName = projectName, onAction = viewModel::onAction, onBack = onBack, onOpenPipeline = onOpenPipeline)
}

@Composable
fun ProjectPipelinesScreen(
    state: ProjectPipelinesState,
    projectName: String,
    onAction: (ProjectPipelinesAction) -> Unit,
    onBack: () -> Unit,
    onOpenPipeline: (pipelineId: Long, ref: String) -> Unit = { _, _ -> },
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("CI / CD", style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.secondary)
            Text("Pipelines", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Recent CI runs for $projectName.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectPipelinesAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.pipelines.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No pipelines yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val now = remember(state.pipelines) { Clock.System.now() }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.pipelines, key = { it.id }) { pipeline ->
                        PipelineRow(pipeline, relativeTime(pipeline.createdAt, now)) { onOpenPipeline(pipeline.id, pipeline.ref) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineRow(pipeline: Pipeline, created: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val visual = pipeline.status.visual()
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
        Icon(visual.icon, contentDescription = pipeline.statusLabel, tint = visual.color, modifier = Modifier.size(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(
                    text = pipeline.ref,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CodeFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            Text(
                text = "#${pipeline.id} · ${pipeline.statusLabel}" + (pipeline.source?.let { " · $it" } ?: "") + " · $created",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
