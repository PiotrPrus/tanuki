package dev.tanuki.feature.mergerequests.domain

/** Lifecycle filter for a project's merge-request list; [apiValue] is GitLab's `state` param. */
enum class MergeRequestFilter(val apiValue: String) {
    OPENED("opened"),
    MERGED("merged"),
    CLOSED("closed"),
    ALL("all"),
}
