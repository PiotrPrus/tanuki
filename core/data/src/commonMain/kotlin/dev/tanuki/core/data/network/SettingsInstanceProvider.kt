package dev.tanuki.core.data.network

import com.russhwolf.settings.Settings
import dev.tanuki.core.domain.config.InstanceProvider

class SettingsInstanceProvider(private val settings: Settings) : InstanceProvider {

    override fun apiBaseUrl(): String {
        val base = settings.getStringOrNull(KEY)?.trimEnd('/') ?: DEFAULT
        return "$base/api/v4/"
    }

    override fun setInstance(baseUrl: String) {
        val trimmed = baseUrl.trim().trimEnd('/')
        val normalized = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        settings.putString(KEY, normalized.ifBlank { DEFAULT })
    }

    private companion object {
        const val KEY = "instance_base_url"
        const val DEFAULT = "https://gitlab.com"
    }
}
