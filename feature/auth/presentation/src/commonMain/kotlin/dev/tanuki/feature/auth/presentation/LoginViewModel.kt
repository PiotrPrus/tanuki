package dev.tanuki.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.auth.domain.AuthRepository
import dev.tanuki.feature.auth.domain.OAuthRedirectHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val redirectHandler: OAuthRedirectHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    // Kept in memory for the duration of a single login attempt.
    private var pendingVerifier: String? = null
    private var pendingState: String? = null

    init {
        // Redirects captured by the platform (Android intent / iOS onOpenURL) flow in here.
        viewModelScope.launch {
            redirectHandler.callbacks.collect { callback ->
                completeLogin(callback.code, callback.state)
            }
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> startLogin()
            is LoginAction.OnRedirect -> completeLogin(action.code, action.state)
            LoginAction.OnUseTokenClick -> _state.update { it.copy(tokenMode = true, error = null) }
            LoginAction.OnUseOAuthClick -> _state.update { it.copy(tokenMode = false, error = null) }
            is LoginAction.OnInstanceChange -> _state.update { it.copy(instanceUrl = action.value) }
            is LoginAction.OnTokenChange -> _state.update { it.copy(token = action.value) }
            LoginAction.OnSubmitToken -> submitToken()
        }
    }

    private fun submitToken() {
        val current = _state.value
        if (!current.canSubmitToken) return
        _state.update { it.copy(isAuthenticating = true, error = null) }
        viewModelScope.launch {
            when (authRepository.loginWithToken(current.instanceUrl.trim(), current.token.trim())) {
                is Result.Success -> {
                    _state.update { it.copy(isAuthenticating = false) }
                    _events.send(LoginEvent.LoggedIn)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isAuthenticating = false, error = TOKEN_FAILED) }
                    _events.send(LoginEvent.ShowError(TOKEN_FAILED))
                }
            }
        }
    }

    private fun startLogin() {
        val request = authRepository.createAuthorizationRequest()
        pendingVerifier = request.codeVerifier
        pendingState = request.state
        _state.update { it.copy(isAuthenticating = true, error = null) }
        viewModelScope.launch {
            _events.send(LoginEvent.LaunchOAuth(request.url))
        }
    }

    private fun completeLogin(code: String?, state: String?) {
        val verifier = pendingVerifier
        if (code == null || verifier == null || state == null || state != pendingState) {
            _state.update { it.copy(isAuthenticating = false, error = OAUTH_FAILED) }
            viewModelScope.launch { _events.send(LoginEvent.ShowError(OAUTH_FAILED)) }
            return
        }
        viewModelScope.launch {
            when (authRepository.exchangeCodeForTokens(code, verifier)) {
                is Result.Success -> {
                    _state.update { it.copy(isAuthenticating = false) }
                    _events.send(LoginEvent.LoggedIn)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isAuthenticating = false, error = OAUTH_FAILED) }
                    _events.send(LoginEvent.ShowError(OAUTH_FAILED))
                }
            }
            pendingVerifier = null
            pendingState = null
        }
    }

    private companion object {
        val OAUTH_FAILED = UiText.Dynamic("Sign-in failed. Please try again.")
        val TOKEN_FAILED = UiText.Dynamic("Couldn't sign in with that token. Check the instance URL and token.")
    }
}
