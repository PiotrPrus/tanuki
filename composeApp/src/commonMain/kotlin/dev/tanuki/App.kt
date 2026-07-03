package dev.tanuki

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.auth.presentation.LoginRoot
import dev.tanuki.feature.mergerequests.presentation.MergeRequestsRoot
import dev.tanuki.navigation.Routes
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        TanukiTheme {
            val navController = rememberNavController()
            val uriHandler = LocalUriHandler.current

            NavHost(
                navController = navController,
                startDestination = Routes.Login,
            ) {
                composable<Routes.Login> {
                    LoginRoot(
                        onLoggedIn = {
                            navController.navigate(Routes.MergeRequests) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        },
                        // Opens the system browser for the OAuth flow. Capturing the
                        // custom-scheme redirect back into the app is the next milestone.
                        onLaunchOAuth = { url -> uriHandler.openUri(url) },
                    )
                }
                composable<Routes.MergeRequests> {
                    MergeRequestsRoot(
                        onOpenInBrowser = { url -> uriHandler.openUri(url) },
                    )
                }
            }
        }
    }
}
