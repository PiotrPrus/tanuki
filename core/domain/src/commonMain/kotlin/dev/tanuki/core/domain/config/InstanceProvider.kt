package dev.tanuki.core.domain.config

/**
 * The GitLab instance the app currently talks to. Defaults to gitlab.com; a Personal
 * Access Token login can point it at any self-hosted instance.
 */
interface InstanceProvider {
    /** e.g. "https://gitlab.com/api/v4/" — used as the API base URL. */
    fun apiBaseUrl(): String

    /** Persist the instance base (e.g. "https://gitlab.example.com"). */
    fun setInstance(baseUrl: String)
}
