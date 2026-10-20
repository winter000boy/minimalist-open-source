package io.github.winter000boy.minimalist.ui

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.data.LauncherApp
import io.github.winter000boy.minimalist.system.openAlarms
import io.github.winter000boy.minimalist.system.openCamera
import io.github.winter000boy.minimalist.system.openDialer
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    homeApps: List<LauncherApp>,
    isDefaultLauncher: Boolean,
    actions: AppActions,
    onSetDefaultLauncher: () -> Unit,
) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Spacer(Modifier.weight(0.5f))
        Clock(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = context::openAlarms,
                ),
        )
        Spacer(Modifier.weight(1f))

        if (homeApps.isEmpty()) {
            Text(
                text = stringResource(R.string.home_empty_hint),
                color = Muted,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
        homeApps.forEach { app ->
            AppRow(app = app, isOnHome = true, actions = actions)
        }
        Spacer(Modifier.weight(1f))

        if (!isDefaultLauncher) {
            Text(
                text = stringResource(R.string.set_default_launcher),
                color = Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSetDefaultLauncher)
                    .padding(12.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = context::openDialer) {
                Icon(painterResource(R.drawable.ic_phone), stringResource(R.string.phone))
            }
            IconButton(onClick = context::openCamera) {
                Icon(painterResource(R.drawable.ic_camera), stringResource(R.string.camera))
            }
        }
    }
}

/** Big time, date underneath. Ticks on the minute while visible. */
@Composable
private fun Clock(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val lifecycleOwner = LocalLifecycleOwner.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(lifecycleOwner) {
        // Restarts on every return to the screen, so the time is never stale after sleep.
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = LocalDateTime.now()
                delay(60_000L - System.currentTimeMillis() % 60_000L)
            }
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatTime(now.toLocalTime(), DateFormat.is24HourFormat(context)),
            fontSize = 76.sp,
        )
        Text(
            text = formatDate(now.toLocalDate(), locale),
            fontSize = 18.sp,
        )
    }
}
