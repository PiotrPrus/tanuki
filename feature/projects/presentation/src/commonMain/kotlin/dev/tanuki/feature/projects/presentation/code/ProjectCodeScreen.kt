package dev.tanuki.feature.projects.presentation.code

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.designsystem.CodeFontFamily
import dev.tanuki.feature.projects.domain.Branch
import dev.tanuki.feature.projects.domain.RepoEntry
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectCodeRoot(
    projectId: Long,
    projectName: String,
    ref: String,
    path: String,
    onBack: () -> Unit,
    onOpenDir: (path: String, name: String) -> Unit,
    onOpenFile: (filePath: String, fileName: String) -> Unit,
    onSwitchBranch: (ref: String) -> Unit,
    viewModel: ProjectCodeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId, ref, path) { viewModel.load(projectId, ref, path) }
    ProjectCodeScreen(
        state = state,
        title = path.substringAfterLast('/').ifBlank { projectName },
        path = path,
        currentRef = ref,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenDir = onOpenDir,
        onOpenFile = onOpenFile,
        onSwitchBranch = onSwitchBranch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCodeScreen(
    state: ProjectCodeState,
    title: String,
    path: String,
    currentRef: String,
    onAction: (ProjectCodeAction) -> Unit,
    onBack: () -> Unit,
    onOpenDir: (path: String, name: String) -> Unit = { _, _ -> },
    onOpenFile: (filePath: String, fileName: String) -> Unit = { _, _ -> },
    onSwitchBranch: (ref: String) -> Unit = {},
) {
    var showBranchSheet by remember { mutableStateOf(false) }
    val branchName = currentRef.ifBlank { state.branches.firstOrNull { it.isDefault }?.name ?: "default" }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("CODE", style = MaterialTheme.typography.labelMedium, fontFamily = CodeFontFamily, color = MaterialTheme.colorScheme.secondary)
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BranchChip(branchName) {
                    showBranchSheet = true
                    onAction(ProjectCodeAction.OnLoadBranches)
                }
                if (path.isNotBlank()) {
                    Text(
                        "/ $path",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onAction(ProjectCodeAction.OnRetry) }) { Text("Retry") }
                }
            }
            state.entries.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Empty folder.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val sorted = remember(state.entries) {
                    state.entries.sortedWith(compareByDescending<RepoEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(sorted, key = { it.path }) { entry ->
                        EntryRow(entry) {
                            if (entry.isDirectory) onOpenDir(entry.path, entry.name) else onOpenFile(entry.path, entry.name)
                        }
                    }
                }
            }
        }
    }

    if (showBranchSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showBranchSheet = false }, sheetState = sheetState) {
            BranchPicker(
                branches = state.branches,
                loading = state.branchesLoading,
                currentBranch = branchName,
                onSelect = { ref ->
                    showBranchSheet = false
                    onSwitchBranch(ref)
                },
            )
        }
    }
}

@Composable
private fun BranchChip(branchName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            branchName,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = CodeFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 180.dp),
        )
        Icon(Icons.Filled.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun BranchPicker(
    branches: List<Branch>,
    loading: Boolean,
    currentBranch: String,
    onSelect: (ref: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
        Text("Switch branch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        when {
            loading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator() }
            branches.isEmpty() -> Text("No branches found.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            else -> LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(branches, key = { it.name }) { branch ->
                    val selected = branch.name == currentBranch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(branch.name) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Filled.AltRoute,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            branch.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = CodeFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (branch.isDefault) {
                            Text("default", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: RepoEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
            contentDescription = null,
            tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = entry.name,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = CodeFontFamily,
            fontWeight = if (entry.isDirectory) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
