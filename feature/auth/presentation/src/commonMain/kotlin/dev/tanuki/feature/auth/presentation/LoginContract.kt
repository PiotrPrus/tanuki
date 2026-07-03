package dev.tanuki.feature.auth.presentation

import dev.tanuki.core.presentation.UiText

data class LoginState(
    val isAuthenticating: Boolean = false,
    val error: UiText? = null,
    /** false = one-tap OAuth (gitlab.com); true = paste a Personal Access Token. */
    val tokenMode: Boolean = false,
    val instanceUrl: String = "https://gitlab.com",
    val token: String = "",
) {
    val canSubmitToken: Boolean get() = instanceUrl.isNotBlank() && token.isNotBlank()
}

sealed interface LoginAction {
    data object OnLoginClick : LoginAction
    /** Delivered by the platform after the OAuth redirect is captured. */
    data class OnRedirect(val code: String?, val state: String?) : LoginAction

    data object OnUseTokenClick : LoginAction
    data object OnUseOAuthClick : LoginAction
    data class OnInstanceChange(val value: String) : LoginAction
    data class OnTokenChange(val value: String) : LoginAction
    data object OnSubmitToken : LoginAction
}

sealed interface LoginEvent {
    /** Ask the platform (Custom Tabs / ASWebAuthenticationSession) to open this URL. */
    data class LaunchOAuth(val url: String) : LoginEvent
    data object LoggedIn : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
}
