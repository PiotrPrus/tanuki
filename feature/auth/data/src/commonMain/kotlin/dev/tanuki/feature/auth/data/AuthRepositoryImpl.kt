package dev.tanuki.feature.auth.data

import dev.tanuki.core.data.network.GitLabConfig
import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.auth.AuthTokens
import dev.tanuki.core.domain.auth.TokenStorage
import dev.tanuki.core.domain.config.InstanceProvider
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.EmptyResult
import dev.tanuki.core.domain.util.asEmptyResult
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.auth.data.dto.TokenResponseDto
import dev.tanuki.feature.auth.data.dto.UserDto
import dev.tanuki.feature.auth.domain.AuthRepository
import dev.tanuki.feature.auth.domain.AuthorizationRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import io.ktor.http.parameters

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val instanceProvider: InstanceProvider,
) : AuthRepository {

    override fun createAuthorizationRequest(): AuthorizationRequest {
        val pkce = PkceGenerator.generate()
        val state = PkceGenerator.randomState()
        val url = buildString {
            append(GitLabConfig.OAUTH_AUTHORIZE_URL)
            append("?client_id=").append(GitLabConfig.CLIENT_ID)
            append("&redirect_uri=").append(GitLabConfig.REDIRECT_URI.encodeURLParameter())
            append("&response_type=code")
            append("&state=").append(state)
            append("&scope=").append(GitLabConfig.SCOPE)
            append("&code_challenge=").append(pkce.codeChallenge)
            append("&code_challenge_method=S256")
        }
        return AuthorizationRequest(url = url, codeVerifier = pkce.codeVerifier, state = state)
    }

    override suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String,
    ): EmptyResult<DataError.Remote> {
        val result = safeCall<TokenResponseDto> {
            httpClient.submitForm(
                url = GitLabConfig.OAUTH_TOKEN_URL,
                formParameters = parameters {
                    append("client_id", GitLabConfig.CLIENT_ID)
                    append("code", code)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", GitLabConfig.REDIRECT_URI)
                    append("code_verifier", codeVerifier)
                },
            )
        }
        return result.map { dto ->
            // OAuth is registered for gitlab.com only.
            instanceProvider.setInstance("https://gitlab.com")
            tokenStorage.saveTokens(
                AuthTokens(
                    accessToken = dto.accessToken,
                    refreshToken = dto.refreshToken,
                    expiresAtEpochSeconds = dto.createdAt + dto.expiresIn,
                ),
            )
        }.asEmptyResult()
    }

    override suspend fun loginWithToken(
        instanceBaseUrl: String,
        token: String,
    ): EmptyResult<DataError.Remote> {
        instanceProvider.setInstance(instanceBaseUrl)
        tokenStorage.clear() // ensure the validation call carries only the explicit token
        val result = safeCall<UserDto> {
            httpClient.get("user") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        return result.map {
            tokenStorage.saveTokens(
                AuthTokens(
                    accessToken = token,
                    refreshToken = "", // PATs don't refresh
                    expiresAtEpochSeconds = Long.MAX_VALUE,
                ),
            )
        }.asEmptyResult()
    }

    override suspend fun isLoggedIn(): Boolean = tokenStorage.hasSession()

    override suspend fun logout() = tokenStorage.clear()
}
