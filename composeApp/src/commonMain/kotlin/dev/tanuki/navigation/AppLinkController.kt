package dev.tanuki.navigation

/**
 * Platform hook for the OS "open supported links" setting. On Android, `gitlab.com` links
 * can only be routed to the app once the user opts in (we can't verify the domain), so we
 * surface a one-tap prompt that jumps to the system screen. Where the concept doesn't apply
 * (iOS), [areGitLabLinksEnabled] returns true so the prompt never shows.
 */
interface AppLinkController {
    /** True if GitLab web links are already routed to this app (or link-routing isn't user-gated). */
    fun areGitLabLinksEnabled(): Boolean

    /** Open the system "Open by default" screen for this app so the user can enable link handling. */
    fun openLinkSettings()
}
