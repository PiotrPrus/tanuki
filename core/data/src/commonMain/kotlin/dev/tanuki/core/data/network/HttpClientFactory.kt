package dev.tanuki.core.data.network

import dev.tanuki.core.domain.auth.AuthTokens
import dev.tanuki.core.domain.auth.TokenStorage
import dev.tanuki.core.domain.config.InstanceProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        engine: HttpClientEngine,
        tokenStorage: TokenStorage,
        instanceProvider: InstanceProvider,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(DefaultRequest) {
                // Resolved per request, so switching instance (e.g. a self-hosted PAT
                // login) takes effect without rebuilding the client.
                url(instanceProvider.apiBaseUrl())
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStorage.getTokens()?.let { tokens ->
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        }
                    }
                    refreshTokens {
                        val current = tokenStorage.getTokens() ?: return@refreshTokens null
                        // Personal Access Token sessions have no refresh token — a 401
                        // means the token was revoked/expired, so force a fresh login.
                        if (current.refreshToken.isBlank()) {
                            tokenStorage.clear()
                            return@refreshTokens null
                        }
                        val response = client.submitForm(
                            url = GitLabConfig.OAUTH_TOKEN_URL,
                            formParameters = parameters {
                                append("client_id", GitLabConfig.CLIENT_ID)
                                append("refresh_token", current.refreshToken)
                                append("grant_type", "refresh_token")
                                append("redirect_uri", GitLabConfig.REDIRECT_URI)
                            },
                        ) {
                            markAsRefreshTokenRequest()
                        }
                        if (!response.status.isSuccess()) {
                            // Refresh failed (e.g. SSO session expired) — force interactive re-login.
                            tokenStorage.clear()
                            return@refreshTokens null
                        }
                        val dto = response.body<TokenRefreshDto>()
                        val tokens = AuthTokens(
                            accessToken = dto.accessToken,
                            refreshToken = dto.refreshToken,
                            expiresAtEpochSeconds = dto.createdAt + dto.expiresIn,
                        )
                        tokenStorage.saveTokens(tokens)
                        BearerTokens(tokens.accessToken, tokens.refreshToken)
                    }
                }
            }
        }
}
