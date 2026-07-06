package dev.tanuki.feature.projects.domain

/** The project scopes shown as tabs on the projects list. */
enum class ProjectFilter {
    /** Repos you belong to, most recently active first. */
    RECENT,

    /** Repos you've starred. */
    STARRED,

    /** Your personal-namespace repos. */
    PERSONAL,

    /** All repos you're a member of, alphabetical. */
    MEMBER,
}
