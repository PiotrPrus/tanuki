package dev.tanuki

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.tanuki.feature.auth.domain.OAuthRedirectHandler
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val redirectHandler: OAuthRedirectHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleRedirect(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "dev.tanuki") {
            redirectHandler.publish(data.toString())
        }
    }
}
