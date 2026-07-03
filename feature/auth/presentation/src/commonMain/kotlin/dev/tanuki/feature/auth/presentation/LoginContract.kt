package dev.tanuki.feature.auth.presentation

import dev.tanuki.core.presentation.UiText

data class LoginState(
    val isAuthenticating: Boolean = false,
    val error: UiText? = null,
)

sealed interface LoginAction {
    data object OnLoginClick : LoginAction
    /** Delivered by the platform after the OAuth redirect is captured. */
    data class OnRedirect(val code: String?, val state: String?) : LoginAction
}

sealed interface LoginEvent {
    /** Ask the platform (Custom Tabs / ASWebAuthenticationSession) to open this URL. */
    data class LaunchOAuth(val url: String) : LoginEvent
    data object LoggedIn : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
}
