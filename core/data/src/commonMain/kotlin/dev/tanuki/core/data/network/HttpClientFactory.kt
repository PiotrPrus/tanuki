package dev.tanuki.core.data.network

import dev.tanuki.core.domain.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine, tokenStorage: TokenStorage): HttpClient =
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
                url(GitLabConfig.API_BASE_URL)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStorage.getTokens()?.let { tokens ->
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        }
                    }
                    // TODO(auth): implement refreshTokens { } against /oauth/token,
                    // falling back to interactive re-login when refresh fails (SSO).
                }
            }
        }
}
