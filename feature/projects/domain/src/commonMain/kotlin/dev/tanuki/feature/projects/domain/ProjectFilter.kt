package dev.tanuki.feature.projects.domain

/** The project scopes shown as tabs on the projects list (mirrors the GitLab /projects filters). */
enum class ProjectFilter {
    ALL,
    STARRED,
    PERSONAL,
    SHARED,
}
