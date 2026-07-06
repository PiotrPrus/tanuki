package dev.tanuki.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** A resolved in-app destination parsed from an external GitLab URL. */
sealed interface DeepLinkTarget {
    /** `gitlab.com/<project path>/-/merge_requests/<iid>`. Path still needs resolving to a numeric id. */
    data class MergeRequest(val projectPath: String, val iid: Long) : DeepLinkTarget
}

/**
 * Bridges an external URL the OS hands to the app (Android VIEW intent / iOS onOpenURL)
 * into the navigation layer. The platform calls [publish] with the raw URL; [App] observes
 * [targets] and navigates. Backed by a buffered channel so a link that arrives during a cold
 * start (before the collector attaches) is still delivered once.
 */
interface DeepLinkHandler {
    val targets: Flow<DeepLinkTarget>
    fun publish(url: String)
}

class DefaultDeepLinkHandler : DeepLinkHandler {
    private val channel = Channel<DeepLinkTarget>(Channel.BUFFERED)
    override val targets: Flow<DeepLinkTarget> = channel.receiveAsFlow()

    override fun publish(url: String) {
        parseGitLabUrl(url)?.let { channel.trySend(it) }
    }
}

/**
 * Parse a gitlab.com web URL into a [DeepLinkTarget], or null if it's not a shape we handle.
 * Splits on the `/-/` separator GitLab puts between a project path and its resource, e.g.
 * `https://gitlab.com/teamtilt/mobile/tiltandroid/-/merge_requests/199`.
 */
fun parseGitLabUrl(url: String): DeepLinkTarget? {
    val path = url.substringAfter("gitlab.com/", missingDelimiterValue = "")
        .ifEmpty { return null }
    val (projectPath, resource) = path.split("/-/", limit = 2)
        .takeIf { it.size == 2 }
        ?.let { it[0].trim('/') to it[1] }
        ?: return null
    if (projectPath.isEmpty()) return null

    return when {
        resource.startsWith("merge_requests/") -> {
            val iid = resource.removePrefix("merge_requests/")
                .substringBefore('/').substringBefore('?').substringBefore('#')
                .toLongOrNull() ?: return null
            DeepLinkTarget.MergeRequest(projectPath, iid)
        }
        else -> null
    }
}
