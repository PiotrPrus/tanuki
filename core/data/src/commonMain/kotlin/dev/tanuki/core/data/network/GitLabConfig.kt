package dev.tanuki.core.data.network

/**
 * gitlab.com OAuth + API configuration.
 *
 * CLIENT_ID: register the app at https://gitlab.com/-/user_settings/applications
 * with "Confidential" UNCHECKED (public/PKCE client) and scope `api`, then paste the
 * Application ID here. See the vault note "GitLab API — OAuth PKCE & Merge Requests".
 */
object GitLabConfig {
    const val HOST = "https://gitlab.com"
    const val API_BASE_URL = "$HOST/api/v4/"

    const val OAUTH_AUTHORIZE_URL = "$HOST/oauth/authorize"
    const val OAUTH_TOKEN_URL = "$HOST/oauth/token"

    // TODO: replace with the real Application ID from gitlab.com.
    const val CLIENT_ID = "REPLACE_WITH_APPLICATION_ID"
    const val REDIRECT_URI = "dev.tanuki://oauth-callback"
    const val SCOPE = "api"
}
