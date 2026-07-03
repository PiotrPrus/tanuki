package dev.tanuki.core.domain.auth

/**
 * Secure persistence for OAuth tokens.
 * Android: EncryptedSharedPreferences (key in Keystore). iOS: Keychain.
 */
interface TokenStorage {
    suspend fun getTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun clear()
    suspend fun hasSession(): Boolean = getTokens() != null
}
