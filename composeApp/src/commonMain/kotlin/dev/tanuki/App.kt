package dev.tanuki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
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
import dev.tanuki.core.domain.util.Result
import dev.tanuki.feature.auth.domain.AuthRepository
import dev.tanuki.feature.projects.domain.ProjectRepository
import dev.tanuki.navigation.AppLinkController
import dev.tanuki.navigation.DeepLinkHandler
import dev.tanuki.navigation.DeepLinkTarget
import dev.tanuki.feature.auth.presentation.LoginRoot
import dev.tanuki.feature.mergerequests.presentation.MergeRequestsRoot
import dev.tanuki.feature.mergerequests.presentation.detail.MergeRequestDetailRoot
import dev.tanuki.feature.mergerequests.presentation.projectlist.ProjectMergeRequestsRoot
import dev.tanuki.feature.projects.presentation.ProjectsRoot
import dev.tanuki.feature.projects.presentation.branches.ProjectBranchesRoot
import dev.tanuki.feature.projects.presentation.dashboard.ProjectDashboardRoot
import dev.tanuki.feature.projects.presentation.groupbrowser.GroupBrowserRoot
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

    // Shared GitLab links (e.g. gitlab.com/group/project/-/merge_requests/199) arrive here.
    // Resolve the project path to a numeric id, then jump straight to the MR. Resolution needs
    // an authenticated session, so a link opened while logged out simply lands on the app
    // (login) without navigating.
    val deepLinkHandler = koinInject<DeepLinkHandler>()
    val projectRepository = koinInject<ProjectRepository>()
    LaunchedEffect(Unit) {
        deepLinkHandler.targets.collect { target ->
            when (target) {
                is DeepLinkTarget.MergeRequest -> {
                    val resolved = projectRepository.resolveProjectId(target.projectPath)
                    if (resolved is Result.Success) {
                        navController.navigate(Routes.MergeRequestDetail(resolved.data, target.iid))
                    }
                }
            }
        }
    }

    // One-time prompt to route gitlab.com links to Tanuki. gitlab.com can't be domain-verified,
    // so Android disables link handling until the user opts in; re-check on resume to hide the
    // prompt right after they return from the settings screen.
    val appLinkController = koinInject<AppLinkController>()
    var linksEnabled by remember { mutableStateOf(appLinkController.areGitLabLinksEnabled()) }
    var linkPromptDismissed by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        linksEnabled = appLinkController.areGitLabLinksEnabled()
    }

    Scaffold(
        bottomBar = {
            if (destination.isTopLevel()) {
                TanukiBottomBar(navController = navController, destination = destination)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (destination.isTopLevel() && !linksEnabled && !linkPromptDismissed) {
                LinkHandlingBanner(
                    onEnable = { appLinkController.openLinkSettings() },
                    onDismiss = { linkPromptDismissed = true },
                )
            }
            NavHost(
                navController = navController,
                startDestination = if (startLoggedIn) Routes.Projects else Routes.Login,
                modifier = Modifier.fillMaxSize(),
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
            composable<Routes.GroupBrowser> { entry ->
                val route = entry.toRoute<Routes.GroupBrowser>()
                GroupBrowserRoot(
                    groupFullPath = route.groupFullPath,
                    onBack = { navController.popBackStack() },
                    onOpenGroup = { fullPath -> navController.openGroup(fullPath) },
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
                    onOpenGroup = { fullPath -> navController.openGroup(fullPath) },
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
}

private fun NavDestination?.isTopLevel(): Boolean =
    this?.hasRoute<Routes.Projects>() == true || this?.hasRoute<Routes.Reviews>() == true

/** Prompts the user to route GitLab links to Tanuki, jumping to the system setting on tap. */
@Composable
private fun LinkHandlingBanner(onEnable: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Link, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text("Open GitLab links in Tanuki", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Turn on \"Open supported links\" so shared merge-request links open here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
            TextButton(
                onClick = onEnable,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) { Text("Enable", fontWeight = FontWeight.Bold) }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss")
            }
        }
    }
}

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

/**
 * Navigate to a group browser hierarchically: if that group is already on the back stack (e.g.
 * reached via a breadcrumb), collapse to it instead of pushing a duplicate. So Back always walks
 * up the group path (…/web → …/ → Projects), never replaying the visit history.
 */
private fun NavHostController.openGroup(fullPath: String) {
    // If this group is already on the back stack (e.g. reached via a breadcrumb), pop back to it
    // — collapsing any detour above it — instead of pushing a duplicate. Otherwise it's a normal
    // drill-down, so push. Either way, Back walks up the group path, never the visit history.
    val poppedToExisting = popBackStack(Routes.GroupBrowser(fullPath), inclusive = false)
    if (!poppedToExisting) {
        navigate(Routes.GroupBrowser(fullPath))
    }
}

private fun NavHostController.switchTab(route: Routes) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
