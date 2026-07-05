package dev.tanuki.feature.projects.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.core.designsystem.tanukiFilterChipColors
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.Visibility
import dev.tanuki.feature.projects.presentation.common.relativeTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ProjectsRoot(
    onOpenProject: (projectId: Long, projectName: String) -> Unit,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectsEvent.OpenDashboard -> onOpenProject(event.projectId, event.projectName)
        }
    }

    ProjectsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ProjectsScreen(
    state: ProjectsState,
    onAction: (ProjectsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            Text("Projects", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${state.visibleProjects.size} " +
                    if (state.visibleProjects.size == 1) "repository" else "repositories",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(ProjectsAction.OnQueryChange(it)) },
            label = { Text("Search projects") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProjectFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onAction(ProjectsAction.OnFilterChange(filter)) },
                    label = { Text(filter.label()) },
                    colors = tanukiFilterChipColors(),
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
            }
            state.visibleProjects.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No projects here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val now = remember(state.visibleProjects) { Clock.System.now() }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.visibleProjects, key = { it.id }) { project ->
                        ProjectCard(project, now) { onAction(ProjectsAction.OnOpen(project)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(project: Project, now: Instant, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val namespace = project.pathWithNamespace.substringBeforeLast('/', missingDelimiterValue = "")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InitialAvatar(project.name)
                Column {
                    if (namespace.isNotEmpty()) {
                        Text(
                            text = namespace.replace("/", " / ") + " /",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            StarBadge(project.starCount)
        }

        project.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VisibilityPill(project.visibility)
            project.lastActivity?.let { iso ->
                val instant = remember(iso) { runCatching { Instant.parse(iso) }.getOrNull() }
                if (instant != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(
                            relativeTime(instant, now),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialAvatar(name: String) {
    val accents = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    val accent = accents[(name.hashCode().rem(accents.size) + accents.size) % accents.size]
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
private fun StarBadge(count: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VisibilityPill(visibility: Visibility) {
    val (label, color) = when (visibility) {
        Visibility.PUBLIC -> "Public" to TanukiTheme.colors.success
        Visibility.INTERNAL -> "Internal" to MaterialTheme.colorScheme.secondary
        Visibility.PRIVATE -> "Private" to MaterialTheme.colorScheme.onSurfaceVariant
        Visibility.UNKNOWN -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = color)
    }
}

private fun formatCount(n: Int): String =
    if (n >= 1000) "${(n / 100) / 10.0}k" else n.toString()

private fun ProjectFilter.label(): String = when (this) {
    ProjectFilter.ALL -> "All"
    ProjectFilter.STARRED -> "Starred"
    ProjectFilter.PERSONAL -> "Personal"
    ProjectFilter.SHARED -> "Shared"
}
