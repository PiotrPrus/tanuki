package dev.tanuki

import androidx.compose.runtime.Composable
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
            val navController = rememberNavController()
            val uriHandler = LocalUriHandler.current

            NavHost(
                navController = navController,
                startDestination = Routes.Login,
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
