package dev.tanuki

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import dev.tanuki.core.designsystem.TanukiTheme
import dev.tanuki.feature.auth.domain.AuthRepository
import dev.tanuki.feature.auth.presentation.LoginRoot
import dev.tanuki.feature.mergerequests.presentation.MergeRequestsRoot
import dev.tanuki.feature.mergerequests.presentation.detail.MergeRequestDetailRoot
import dev.tanuki.feature.mergerequests.presentation.projectlist.ProjectMergeRequestsRoot
import dev.tanuki.feature.projects.presentation.ProjectsRoot
import dev.tanuki.feature.projects.presentation.branches.ProjectBranchesRoot
import dev.tanuki.feature.projects.presentation.dashboard.ProjectDashboardRoot
import dev.tanuki.feature.projects.presentation.code.FileViewRoot
import dev.tanuki.feature.projects.presentation.code.ProjectCodeRoot
import dev.tanuki.feature.projects.presentation.pipelines.PipelineDetailRoot
import dev.tanuki.feature.projects.presentation.pipelines.ProjectPipelinesRoot
import dev.tanuki.feature.projects.presentation.refdetail.RefDetailRoot
import dev.tanuki.feature.projects.presentation.releases.ProjectReleasesRoot
import dev.tanuki.feature.projects.presentation.tags.ProjectTagsRoot
import dev.tanuki.navigation.Routes
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        // Load images (GitLab uploads) through the authenticated Ktor client so private
        // project media resolves with the bearer token.
        val imageHttpClient = koinInject<HttpClient>()
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory(httpClient = imageHttpClient)) }
                .build()
        }
        TanukiTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val authRepository = koinInject<AuthRepository>()
                // Restore the persisted session on launch — skip Login if a token is stored.
                val loggedIn by produceState<Boolean?>(initialValue = null) {
                    value = authRepository.isLoggedIn()
                }

                when (val isLoggedIn = loggedIn) {
                    null -> Box(
                        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                    else -> AppScaffold(startLoggedIn = isLoggedIn)
                }
            }
        }
    }
}

@Composable
private fun AppScaffold(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination

    Scaffold(
        bottomBar = {
            if (destination.isTopLevel()) {
                TanukiBottomBar(navController = navController, destination = destination)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startLoggedIn) Routes.Projects else Routes.Login,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable<Routes.Login> {
                LoginRoot(
                    onLoggedIn = {
                        navController.navigate(Routes.Projects) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    },
                    onLaunchOAuth = { url -> uriHandler.openUri(url) },
                )
            }
            composable<Routes.Projects> {
                ProjectsRoot(
                    onOpenProject = { projectId, projectName ->
                        navController.navigate(Routes.ProjectDashboard(projectId, projectName))
                    },
                )
            }
            composable<Routes.ProjectDashboard> { entry ->
                val route = entry.toRoute<Routes.ProjectDashboard>()
                ProjectDashboardRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenMergeRequests = { projectId, projectName ->
                        navController.navigate(Routes.ProjectMergeRequests(projectId, projectName))
                    },
                    onOpenBranches = { projectId, projectName ->
                        navController.navigate(Routes.ProjectBranches(projectId, projectName))
                    },
                    onOpenTags = { projectId, projectName ->
                        navController.navigate(Routes.ProjectTags(projectId, projectName))
                    },
                    onOpenReleases = { projectId, projectName ->
                        navController.navigate(Routes.ProjectReleases(projectId, projectName))
                    },
                    onOpenPipelines = { projectId, projectName ->
                        navController.navigate(Routes.ProjectPipelines(projectId, projectName))
                    },
                    onOpenCode = { projectId, projectName, ref ->
                        navController.navigate(Routes.ProjectCode(projectId, projectName, ref, ""))
                    },
                    onOpenInBrowser = { url -> uriHandler.openUri(url) },
                )
            }
            composable<Routes.ProjectCode> { entry ->
                val route = entry.toRoute<Routes.ProjectCode>()
                ProjectCodeRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    ref = route.ref,
                    path = route.path,
                    onBack = { navController.popBackStack() },
                    onOpenDir = { path, _ ->
                        navController.navigate(Routes.ProjectCode(route.projectId, route.projectName, route.ref, path))
                    },
                    onOpenFile = { filePath, fileName ->
                        navController.navigate(Routes.FileView(route.projectId, route.ref, filePath, fileName))
                    },
                    onSwitchBranch = { newRef ->
                        navController.navigate(Routes.ProjectCode(route.projectId, route.projectName, newRef, ""))
                    },
                )
            }
            composable<Routes.FileView> { entry ->
                val route = entry.toRoute<Routes.FileView>()
                FileViewRoot(
                    projectId = route.projectId,
                    ref = route.ref,
                    filePath = route.filePath,
                    fileName = route.fileName,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Routes.ProjectPipelines> { entry ->
                val route = entry.toRoute<Routes.ProjectPipelines>()
                ProjectPipelinesRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenPipeline = { pipelineId, ref ->
                        navController.navigate(Routes.PipelineDetail(route.projectId, pipelineId, ref))
                    },
                )
            }
            composable<Routes.PipelineDetail> { entry ->
                val route = entry.toRoute<Routes.PipelineDetail>()
                PipelineDetailRoot(
                    projectId = route.projectId,
                    pipelineId = route.pipelineId,
                    title = "${route.title} · #${route.pipelineId}",
                    onBack = { navController.popBackStack() },
                    onOpenInBrowser = { url -> uriHandler.openUri(url) },
                )
            }
            composable<Routes.ProjectTags> { entry ->
                val route = entry.toRoute<Routes.ProjectTags>()
                ProjectTagsRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenTag = { ref, fromRef, title ->
                        navController.navigate(Routes.RefDetail(route.projectId, ref, fromRef, title, isRelease = false))
                    },
                )
            }
            composable<Routes.ProjectReleases> { entry ->
                val route = entry.toRoute<Routes.ProjectReleases>()
                ProjectReleasesRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenRelease = { ref, fromRef, title ->
                        navController.navigate(Routes.RefDetail(route.projectId, ref, fromRef, title, isRelease = true))
                    },
                )
            }
            composable<Routes.RefDetail> { entry ->
                val route = entry.toRoute<Routes.RefDetail>()
                RefDetailRoot(
                    projectId = route.projectId,
                    ref = route.ref,
                    fromRef = route.fromRef,
                    title = route.title,
                    isRelease = route.isRelease,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Routes.ProjectBranches> { entry ->
                val route = entry.toRoute<Routes.ProjectBranches>()
                ProjectBranchesRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenMergeRequest = { projectId, iid ->
                        navController.navigate(Routes.MergeRequestDetail(projectId, iid))
                    },
                    onOpenInBrowser = { url -> uriHandler.openUri(url) },
                )
            }
            composable<Routes.ProjectMergeRequests> { entry ->
                val route = entry.toRoute<Routes.ProjectMergeRequests>()
                ProjectMergeRequestsRoot(
                    projectId = route.projectId,
                    projectName = route.projectName,
                    onBack = { navController.popBackStack() },
                    onOpenMergeRequest = { projectId, iid ->
                        navController.navigate(Routes.MergeRequestDetail(projectId, iid))
                    },
                )
            }
            composable<Routes.Reviews> {
                MergeRequestsRoot(
                    onOpenMergeRequest = { projectId, iid ->
                        navController.navigate(Routes.MergeRequestDetail(projectId, iid))
                    },
                )
            }
            composable<Routes.MergeRequestDetail> { entry ->
                val route = entry.toRoute<Routes.MergeRequestDetail>()
                MergeRequestDetailRoot(
                    projectId = route.projectId,
                    iid = route.iid,
                    onBack = { navController.popBackStack() },
                    onOpenInBrowser = { url -> uriHandler.openUri(url) },
                )
            }
        }
    }
}

private fun NavDestination?.isTopLevel(): Boolean =
    this?.hasRoute<Routes.Projects>() == true || this?.hasRoute<Routes.Reviews>() == true

@Composable
private fun TanukiBottomBar(navController: NavHostController, destination: NavDestination?) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    )
    NavigationBar {
        NavigationBarItem(
            selected = destination?.hasRoute<Routes.Reviews>() == true,
            onClick = { navController.switchTab(Routes.Reviews) },
            icon = { Icon(Icons.Filled.MergeType, contentDescription = null) },
            label = { Text("Reviews") },
            colors = colors,
        )
        NavigationBarItem(
            selected = destination?.hasRoute<Routes.Projects>() == true,
            onClick = { navController.switchTab(Routes.Projects) },
            icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
            label = { Text("Projects") },
            colors = colors,
        )
    }
}

private fun NavHostController.switchTab(route: Routes) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
