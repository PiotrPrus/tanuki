package dev.tanuki.feature.auth.domain

import kotlinx.coroutines.flow.Flow

/** The parsed contents of an OAuth redirect (`dev.tanuki://oauth-callback?code=...&state=...`). */
data class OAuthCallback(
    val code: String?,
    val state: String?,
)

/**
 * Bridges the platform OAuth redirect (Android intent / iOS onOpenURL) into the shared
 * login flow. The platform calls [publish] with the raw redirect URI; the ViewModel
 * observes [callbacks].
 */
interface OAuthRedirectHandler {
    val callbacks: Flow<OAuthCallback>
    fun publish(uri: String)
}
