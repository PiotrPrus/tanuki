package dev.tanuki.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Login : Routes

    @Serializable
    data object Projects : Routes

    @Serializable
    data object MergeRequests : Routes
}
