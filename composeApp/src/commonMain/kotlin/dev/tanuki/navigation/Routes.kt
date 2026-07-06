package dev.tanuki.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Login : Routes

    @Serializable
    data object Projects : Routes

    /**
     * Browse a group's subgroups and projects. [groupFullPath] is the GitLab path, e.g.
     * "teamtilt/web". Keyed by path only (no title) so re-entering the same group can collapse
     * the back stack to it — the display name is loaded by the screen.
     */
    @Serializable
    data class GroupBrowser(val groupFullPath: String) : Routes

    @Serializable
    data class ProjectDashboard(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectMergeRequests(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectBranches(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectTags(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectReleases(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectPipelines(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectCode(val projectId: Long, val projectName: String, val ref: String, val path: String) : Routes

    @Serializable
    data class FileView(val projectId: Long, val ref: String, val filePath: String, val fileName: String) : Routes

    @Serializable
    data class PipelineDetail(val projectId: Long, val pipelineId: Long, val title: String) : Routes

    /** Tag/release detail: [ref] is the tag; [fromRef] is the previous tag to diff against (null = oldest). */
    @Serializable
    data class RefDetail(
        val projectId: Long,
        val ref: String,
        val fromRef: String?,
        val title: String,
        val isRelease: Boolean,
    ) : Routes

    @Serializable
    data object Reviews : Routes

    @Serializable
    data class MergeRequestDetail(val projectId: Long, val iid: Long) : Routes
}
