package io.github.winter000boy.minimalist.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.data.LauncherApp

/** Everything the long-press menu (and a tap) can do to an app. */
class AppActions(
    val launch: (LauncherApp) -> Unit,
    val addToHome: (LauncherApp) -> Unit,
    val removeFromHome: (LauncherApp) -> Unit,
    val openAppInfo: (LauncherApp) -> Unit,
    val uninstall: (LauncherApp) -> Unit,
)

/** An app name, no icon. Tap to open, long-press for the menu. */
@Composable
fun AppRow(
    app: LauncherApp,
    isOnHome: Boolean,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Box(modifier) {
        Text(
            text = app.label,
            fontSize = 26.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { actions.launch(app) },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen = true
                    },
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
        )
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            offset = DpOffset(24.dp, 0.dp),
        ) {
            @Composable
            fun item(label: Int, action: (LauncherApp) -> Unit) = DropdownMenuItem(
                text = { Text(stringResource(label)) },
                onClick = {
                    menuOpen = false
                    action(app)
                },
            )
            if (isOnHome) {
                item(R.string.remove_from_home, actions.removeFromHome)
            } else {
                item(R.string.add_to_home, actions.addToHome)
            }
            item(R.string.app_info, actions.openAppInfo)
            if (app.canUninstall) item(R.string.uninstall, actions.uninstall)
        }
    }
}
