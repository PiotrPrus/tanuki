package dev.tanuki.feature.projects.presentation.branches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.projects.domain.Branch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Composable
fun ProjectBranchesRoot(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    onOpenMergeRequest: (projectId: Long, iid: Long) -> Unit,
    onOpenInBrowser: (url: String) -> Unit,
    viewModel: ProjectBranchesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreateSheet by remember { mutableStateOf(false) }
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProjectBranchesEvent.OpenInBrowser -> onOpenInBrowser(event.url)
            is ProjectBranchesEvent.OpenMergeRequest -> onOpenMergeRequest(event.projectId, event.iid)
            ProjectBranchesEvent.BranchCreated -> showCreateSheet = false
        }
    }
    ProjectBranchesScreen(
        state = state,
        projectName = projectName,
        showCreateSheet = showCreateSheet,
        onShowCreateSheet = { showCreateSheet = it },
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBranchesScreen(
    state: ProjectBranchesState,
    projectName: String,
    showCreateSheet: Boolean,
    onShowCreateSheet: (Boolean) -> Unit,
    onAction: (ProjectBranchesAction) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹ Back") }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "REPOSITORY",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = CodeFontFamily,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "Branches",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Development streams for $projectName.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { onAction(ProjectBranchesAction.OnRetry) }) { Text("Retry") }
                    }
                }
                state.branches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No branches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> BranchList(state.branches, onAction)
            }
        }

        FloatingActionButton(
            onClick = { onShowCreateSheet(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New branch")
        }

        if (showCreateSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { onShowCreateSheet(false) },
                sheetState = sheetState,
            ) {
                CreateBranchSheet(
                    branches = state.branches,
                    isCreating = state.isCreating,
                    error = state.createError?.asString(),
                    onCreate = { name, source -> onAction(ProjectBranchesAction.OnCreateBranch(name, source)) },
                )
            }
        }
    }
}

@Composable
private fun BranchList(
    branches: List<Branch>,
    onAction: (ProjectBranchesAction) -> Unit,
) {
    val now = remember(branches) { Clock.System.now() }
    val staleThreshold = 90.days
    val (mine, active, stale) = remember(branches, now) {
        val stale = branches.filter { now - it.lastActivity > staleThreshold && !it.isDefault }
        val fresh = branches - stale.toSet()
        val mine = fresh.filter { it.isMine && !it.isDefault }
            .sortedByDescending { it.lastActivity }
        val active = (fresh - mine.toSet())
            .sortedWith(compareByDescending<Branch> { it.isDefault }.thenByDescending { it.lastActivity })
        Triple(mine, active, stale.sortedByDescending { it.lastActivity })
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (mine.isNotEmpty()) {
            item(key = "h-mine") { SectionHeader("Mine", mine.size, muted = false) }
            items(mine, key = { "m-${it.name}" }) { branch ->
                MineBranchCard(branch, now, onAction)
            }
        }
        if (active.isNotEmpty()) {
            item(key = "h-active") { SectionHeader("Active", active.size, muted = false) }
            items(active, key = { "a-${it.name}" }) { branch ->
                BranchRow(branch, now, muted = false, onAction = onAction)
            }
        }
        if (stale.isNotEmpty()) {
            item(key = "h-stale") { SectionHeader("Stale", stale.size, muted = true) }
            items(stale, key = { "s-${it.name}" }) { branch ->
                BranchRow(branch, now, muted = true, onAction = onAction)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, muted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .padding(horizontal = 8.dp, vertical = 1.dp),
        ) {
            Text(count.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MineBranchCard(
    branch: Branch,
    now: Instant,
    onAction: (ProjectBranchesAction) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .clickable { onAction(ProjectBranchesAction.OnOpenBranch(branch)) }
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    val category = categoryFor(branch.name)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(category.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = CodeFontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Avatar(branch.authorAvatarUrl, branch.lastCommitAuthor, size = 28)
            }
            UpdatedRow(branch, now, showAvatar = false, showAuthor = false, modifier = Modifier.padding(top = 16.dp))
            branch.openMergeRequestIid?.let { iid ->
                MrOpenTag(onClick = { onAction(ProjectBranchesAction.OnOpenMergeRequest(iid)) }, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable
private fun BranchRow(
    branch: Branch,
    now: Instant,
    muted: Boolean,
    onAction: (ProjectBranchesAction) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onAction(ProjectBranchesAction.OnOpenBranch(branch)) }
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (muted) 0.5f else 1f), shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (muted) Icons.Filled.History else Icons.Filled.AltRoute,
                contentDescription = null,
                tint = when {
                    muted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    branch.isDefault -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = branch.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CodeFontFamily,
                    color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (branch.isDefault) TinyChip("Default")
                if (branch.isProtected) {
                    Icon(Icons.Filled.Lock, contentDescription = "Protected", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
            UpdatedRow(branch, now, showAvatar = true, showAuthor = true, modifier = Modifier.padding(top = 4.dp))
            if (!muted) branch.openMergeRequestIid?.let { iid ->
                MrOpenTag(onClick = { onAction(ProjectBranchesAction.OnOpenMergeRequest(iid)) }, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun UpdatedRow(
    branch: Branch,
    now: Instant,
    showAvatar: Boolean,
    showAuthor: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (showAvatar) Avatar(branch.authorAvatarUrl, branch.lastCommitAuthor, size = 18)
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(
            text = "updated ${relativeTime(branch.lastActivity, now)}" +
                (branch.lastCommitAuthor?.takeIf { showAuthor }?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MrOpenTag(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.CallMerge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            "MR OPEN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = CodeFontFamily,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun Avatar(url: String?, name: String?, size: Int) {
    val shape = CircleShape
    val mod = Modifier.size(size.dp).clip(shape)
    if (!url.isNullOrBlank()) {
        AsyncImage(model = url, contentDescription = name, modifier = mod.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape))
    } else {
        Box(modifier = mod.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            val initial = name?.trim()?.firstOrNull()?.uppercaseChar()
            if (initial != null) {
                Text(initial.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size((size * 0.6f).dp))
            }
        }
    }
}

@Composable
private fun TinyChip(label: String) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@Composable
private fun CreateBranchSheet(
    branches: List<Branch>,
    isCreating: Boolean,
    error: String?,
    onCreate: (name: String, source: String) -> Unit,
) {
    val names = remember(branches) { branches.map { it.name } }
    var name by remember { mutableStateOf("") }
    var source by remember(branches) { mutableStateOf(branches.firstOrNull { it.isDefault }?.name ?: names.firstOrNull().orEmpty()) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp)) {
        Text("New branch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Text("Create from", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Text(source, modifier = Modifier.weight(1f), fontFamily = CodeFontFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.UnfoldMore, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                names.forEach { n ->
                    DropdownMenuItem(text = { Text(n, fontFamily = CodeFontFamily) }, onClick = { source = n; expanded = false })
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("New branch name") },
            placeholder = { Text("feat/my-change") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )

        if (error != null) {
            Text(error, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = { onCreate(name, source) },
            enabled = name.isNotBlank() && source.isNotBlank() && !isCreating,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            if (isCreating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Create branch")
            }
        }
    }
}

private data class BranchCategory(val label: String, val icon: ImageVector)

private fun categoryFor(name: String): BranchCategory =
    when (name.substringBefore('/', "").lowercase()) {
        "feat", "feature" -> BranchCategory("Feature", Icons.Filled.Bolt)
        "fix", "bugfix", "hotfix" -> BranchCategory("Fix", Icons.Filled.BugReport)
        "chore" -> BranchCategory("Chore", Icons.Filled.Build)
        "refactor" -> BranchCategory("Refactor", Icons.Filled.Sync)
        "release" -> BranchCategory("Release", Icons.Filled.RocketLaunch)
        "docs" -> BranchCategory("Docs", Icons.Filled.Description)
        else -> BranchCategory("Working", Icons.Filled.Bolt)
    }

/** Compact "5m ago" / "3d ago" / "4mo ago" from [instant] relative to [now]. */
private fun relativeTime(instant: Instant, now: Instant): String {
    val d = now - instant
    return when {
        d.inWholeMinutes < 1 -> "just now"
        d.inWholeHours < 1 -> "${d.inWholeMinutes}m ago"
        d.inWholeDays < 1 -> "${d.inWholeHours}h ago"
        d.inWholeDays < 7 -> "${d.inWholeDays}d ago"
        d.inWholeDays < 30 -> "${d.inWholeDays / 7}w ago"
        d.inWholeDays < 365 -> "${d.inWholeDays / 30}mo ago"
        else -> "${d.inWholeDays / 365}y ago"
    }
}
