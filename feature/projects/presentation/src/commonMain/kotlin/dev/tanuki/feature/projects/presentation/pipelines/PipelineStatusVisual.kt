package dev.tanuki.feature.projects.presentation.pipelines

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.projects.domain.PipelineStatus

data class StatusVisual(val icon: ImageVector, val color: Color)

@Composable
fun PipelineStatus.visual(): StatusVisual = when (this) {
    PipelineStatus.SUCCESS -> StatusVisual(Icons.Filled.CheckCircle, TanukiTheme.colors.success)
    PipelineStatus.FAILED -> StatusVisual(Icons.Filled.Cancel, MaterialTheme.colorScheme.error)
    PipelineStatus.RUNNING -> StatusVisual(Icons.Filled.Autorenew, MaterialTheme.colorScheme.primary)
    PipelineStatus.OTHER -> StatusVisual(Icons.Filled.PauseCircleOutline, MaterialTheme.colorScheme.onSurfaceVariant)
}
