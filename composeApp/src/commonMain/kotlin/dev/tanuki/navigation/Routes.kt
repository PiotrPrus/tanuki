package dev.tanuki.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Login : Routes

    @Serializable
    data object Projects : Routes

    @Serializable
    data class ProjectDashboard(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data class ProjectMergeRequests(val projectId: Long, val projectName: String) : Routes

    @Serializable
    data object Reviews : Routes

    @Serializable
    data class MergeRequestDetail(val projectId: Long, val iid: Long) : Routes
}
