package dev.tanuki

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.auth.presentation.LoginRoot
import dev.tanuki.feature.mergerequests.presentation.MergeRequestsRoot
import dev.tanuki.feature.projects.presentation.ProjectsRoot
import dev.tanuki.navigation.Routes
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        TanukiTheme {
            // Background fills edge-to-edge (behind the system bars); content is inset so
            // it clears the status bar and the bottom navigation bar.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                val navController = rememberNavController()
                val uriHandler = LocalUriHandler.current

                NavHost(
                    navController = navController,
                    startDestination = Routes.Login,
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
                ) {
                composable<Routes.Login> {
                    LoginRoot(
                        onLoggedIn = {
                            navController.navigate(Routes.Projects) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        },
                        // Opens the system browser for OAuth; the redirect is captured by
                        // the platform (Android intent / iOS onOpenURL) → OAuthRedirectHandler.
                        onLaunchOAuth = { url -> uriHandler.openUri(url) },
                    )
                }
                composable<Routes.Projects> {
                    ProjectsRoot(
                        onOpenInBrowser = { url -> uriHandler.openUri(url) },
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
}
