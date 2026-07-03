package dev.tanuki.core.data.auth

import com.russhwolf.settings.Settings
import dev.tanuki.core.domain.auth.AuthTokens
import dev.tanuki.core.domain.auth.TokenStorage

/**
 * [TokenStorage] backed by multiplatform-settings. The concrete [Settings] instance is
 * provided per platform: EncryptedSharedPreferences on Android, Keychain on iOS.
 */
class SettingsTokenStorage(private val settings: Settings) : TokenStorage {

    override suspend fun getTokens(): AuthTokens? {
        val access = settings.getStringOrNull(KEY_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        return AuthTokens(
            accessToken = access,
            refreshToken = refresh,
            expiresAtEpochSeconds = settings.getLong(KEY_EXPIRES_AT, 0L),
        )
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        settings.putString(KEY_ACCESS, tokens.accessToken)
        settings.putString(KEY_REFRESH, tokens.refreshToken)
        settings.putLong(KEY_EXPIRES_AT, tokens.expiresAtEpochSeconds)
    }

    override suspend fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
        settings.remove(KEY_EXPIRES_AT)
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
