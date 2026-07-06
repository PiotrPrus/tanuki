package dev.tanuki.navigation

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.provider.Settings

/**
 * Reads/opens the Android "Open by default" link settings. `gitlab.com` can't be domain-verified
 * by us, so it starts disabled; this lets us prompt the user and jump straight to the toggle.
 */
class AndroidAppLinkController(private val context: Context) : AppLinkController {

    override fun areGitLabLinksEnabled(): Boolean {
        val manager = context.getSystemService(DomainVerificationManager::class.java) ?: return true
        val state = runCatching { manager.getDomainVerificationUserState(context.packageName) }
            .getOrNull() ?: return true
        val hostState = state.hostToStateMap["gitlab.com"] ?: return false
        return hostState == DomainVerificationUserState.DOMAIN_STATE_SELECTED ||
            hostState == DomainVerificationUserState.DOMAIN_STATE_VERIFIED
    }

    override fun openLinkSettings() {
        val intent = Intent(
            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
