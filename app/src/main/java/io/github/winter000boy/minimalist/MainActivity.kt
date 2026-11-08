package io.github.winter000boy.minimalist

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.winter000boy.minimalist.ui.LauncherScreen
import io.github.winter000boy.minimalist.ui.MinimalistTheme
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

    private val homePresses = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            MinimalistTheme(font = settings.font, textSize = settings.textSize) {
                LauncherScreen(homePresses = homePresses, viewModel = viewModel)
            }
        }
    }

    /** As a singleTask home activity, pressing Home while we're showing arrives here. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) homePresses.tryEmit(Unit)
    }
}
