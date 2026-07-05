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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.PipelineJob
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PipelineDetailRoot(
    projectId: Long,
    pipelineId: Long,
    title: String,
    onBack: () -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: PipelineDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(pipelineId) { viewModel.load(projectId, pipelineId, title) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PipelineDetailEvent.OpenInBrowser -> onOpenInBrowser(event.url)
        }
    }
    PipelineDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun PipelineDetailScreen(
    state: PipelineDetailState,
    onAction: (PipelineDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("PIPELINE", style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.secondary)
            Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(PipelineDetailAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.jobs.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No jobs in this pipeline.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val stages = state.jobs.groupBy { it.stage }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    stages.forEach { (stage, jobs) ->
                        item(key = "stage-$stage") { StageHeader(stage, jobs.size) }
                        items(jobs.size, key = { jobs[it].id }) { i ->
                            JobRow(jobs[i]) { onAction(PipelineDetailAction.OnOpenJob(jobs[i])) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageHeader(stage: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
    ) {
        Text(stage.ifBlank { "stage" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("$count", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun JobRow(job: PipelineJob, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val visual = job.status.visual()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(visual.icon, contentDescription = job.statusLabel, tint = visual.color, modifier = Modifier.size(20.dp))
        Text(
            text = job.name,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = CodeFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (job.allowFailure) {
            Text("allowed to fail", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(job.statusLabel, style = MaterialTheme.typography.labelMedium, color = visual.color)
    }
}
