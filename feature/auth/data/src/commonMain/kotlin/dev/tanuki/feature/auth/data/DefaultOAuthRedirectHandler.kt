package dev.tanuki.feature.auth.data

import dev.tanuki.feature.auth.domain.OAuthCallback
import dev.tanuki.feature.auth.domain.OAuthRedirectHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DefaultOAuthRedirectHandler : OAuthRedirectHandler {

    // extraBufferCapacity = 1 so tryEmit succeeds even if the redirect arrives before
    // the ViewModel starts collecting (activity recreated during the browser round-trip).
    private val _callbacks = MutableSharedFlow<OAuthCallback>(extraBufferCapacity = 1)
    override val callbacks = _callbacks.asSharedFlow()

    override fun publish(uri: String) {
        val query = uri.substringAfter('?', "")
        val params = query.split('&')
            .mapNotNull { pair ->
                val i = pair.indexOf('=')
                if (i < 0) null else pair.substring(0, i) to pair.substring(i + 1)
            }
            .toMap()
        _callbacks.tryEmit(OAuthCallback(code = params["code"], state = params["state"]))
    }
}
