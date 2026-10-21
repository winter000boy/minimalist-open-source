package io.github.winter000boy.minimalist.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.winter000boy.minimalist.LauncherViewModel
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.system.homeRoleRequestIntent
import io.github.winter000boy.minimalist.system.isDefaultLauncher
import io.github.winter000boy.minimalist.system.launchApp
import io.github.winter000boy.minimalist.system.openAppInfo
import io.github.winter000boy.minimalist.system.openHomeSettings
import io.github.winter000boy.minimalist.system.uninstall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// The drawer sits to the left of home, so swiping right reveals it.
private const val DRAWER_PAGE = 0
private const val HOME_PAGE = 1

@OptIn(ExperimentalFoundationApi::class) // LocalBringIntoViewSpec
@Composable
fun LauncherScreen(
    homePresses: Flow<Unit>,
    viewModel: LauncherViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val homeApps by viewModel.homeApps.collectAsStateWithLifecycle()
    // Collecting only while resumed re-reads it on every return to the launcher.
    val screenTime by viewModel.screenTime.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val homeAppKeys = remember(homeApps) { homeApps.mapTo(HashSet()) { it.key } }
    val pagerState = rememberPagerState(initialPage = HOME_PAGE) { 2 }
    var query by rememberSaveable { mutableStateOf("") }

    var isDefaultLauncher by remember { mutableStateOf(context.isDefaultLauncher()) }
    LifecycleResumeEffect(Unit) {
        isDefaultLauncher = context.isDefaultLauncher()
        onPauseOrDispose {}
    }
    val homeRoleRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        isDefaultLauncher = context.isDefaultLauncher()
        // Android stops showing the dialog after it has been declined twice.
        if (!isDefaultLauncher) context.openHomeSettings()
    }

    // Off the drawer, its search field may not take focus, so the keyboard can't pop up over home.
    var searchFocusable by remember { mutableStateOf(false) }

    fun goHome(animate: Boolean) {
        searchFocusable = false
        query = ""
        focusManager.clearFocus()
        scope.launch {
            if (animate) pagerState.animateScrollToPage(HOME_PAGE) else pagerState.scrollToPage(HOME_PAGE)
        }
    }

    // Home button while the drawer is open.
    LaunchedEffect(homePresses) { homePresses.collect { goHome(animate = true) } }
    // Coming back from an app, or waking the phone, always lands on the clock.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { goHome(animate = false) }
    // Leaving the drawer by any route drops the search and the keyboard.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            searchFocusable = page == DRAWER_PAGE
            if (page == HOME_PAGE) {
                query = ""
                focusManager.clearFocus()
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage == DRAWER_PAGE) { goHome(animate = true) }
    // As the home app there is nothing behind us to go back to.
    BackHandler(enabled = pagerState.currentPage == HOME_PAGE && isDefaultLauncher) {}

    val resources = LocalResources.current
    val actions = remember(context, resources, viewModel) {
        AppActions(
            launch = { app ->
                focusManager.clearFocus()
                context.launchApp(app)
            },
            addToHome = { app ->
                if (!viewModel.addToHome(app)) {
                    val max = LauncherViewModel.MAX_HOME_APPS
                    val message = resources.getQuantityString(R.plurals.home_full, max, max)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            removeFromHome = viewModel::removeFromHome,
            openAppInfo = context::openAppInfo,
            uninstall = context::uninstall,
        )
    }

    // Only a swipe or goHome() may change pages. Otherwise anything that gains focus in the
    // off-screen drawer (Android re-focuses the first focusable item after a key press) asks the
    // pager to bring it into view and scrolls straight back to the drawer. Inside each page,
    // bring-into-view works as normal, so the app list still follows keyboard focus.
    val defaultBringIntoViewSpec = LocalBringIntoViewSpec.current
    CompositionLocalProvider(LocalBringIntoViewSpec provides NeverBringIntoView) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            CompositionLocalProvider(LocalBringIntoViewSpec provides defaultBringIntoViewSpec) {
                when (page) {
                    DRAWER_PAGE -> AppDrawer(
                        apps = apps,
                        homeAppKeys = homeAppKeys,
                        query = query,
                        onQueryChange = { query = it },
                        actions = actions,
                        searchFocusable = searchFocusable,
                    )
                    else -> HomeScreen(
                        homeApps = homeApps,
                        screenTime = screenTime,
                        isDefaultLauncher = isDefaultLauncher,
                        actions = actions,
                        onSetDefaultLauncher = {
                            val intent = context.homeRoleRequestIntent()
                            if (intent != null) homeRoleRequest.launch(intent) else context.openHomeSettings()
                        },
                    )
                }
            }
        }
    }
}

private val NeverBringIntoView = object : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float) = 0f
}
