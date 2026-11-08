package io.github.winter000boy.minimalist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.data.AppFont
import io.github.winter000boy.minimalist.data.LauncherSettings
import io.github.winter000boy.minimalist.data.TextSize
import io.github.winter000boy.minimalist.system.openDisplaySettings
import io.github.winter000boy.minimalist.system.openHomeSettings

@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    onChange: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back))
            }
            Text(stringResource(R.string.settings), fontSize = 28.sp)
        }

        SectionLabel(R.string.font)
        AppFont.entries.forEach { font ->
            Choice(
                label = stringResource(font.label),
                selected = settings.font == font,
                fontFamily = font.fontFamily(),
                onClick = { onChange { it.copy(font = font) } },
            )
        }

        SectionLabel(R.string.text_size)
        TextSize.entries.forEach { size ->
            Choice(
                label = stringResource(size.label),
                selected = settings.textSize == size,
                onClick = { onChange { it.copy(textSize = size) } },
            )
        }

        SectionLabel(R.string.gestures)
        Text(
            text = stringResource(R.string.double_tap_to_wake),
            fontSize = 22.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Hint(R.string.double_tap_to_wake_info, onClick = context::openDisplaySettings)

        SectionLabel(R.string.home_app)
        Text(
            text = stringResource(R.string.change_home_app),
            fontSize = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = context::openHomeSettings)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

private val AppFont.label
    get() = when (this) {
        AppFont.Serif -> R.string.font_serif
        AppFont.Sans -> R.string.font_sans
        AppFont.Light -> R.string.font_light
        AppFont.Condensed -> R.string.font_condensed
        AppFont.Mono -> R.string.font_mono
    }

private val TextSize.label
    get() = when (this) {
        TextSize.Small -> R.string.text_size_small
        TextSize.Default -> R.string.text_size_default
        TextSize.Large -> R.string.text_size_large
        TextSize.ExtraLarge -> R.string.text_size_extra_large
    }

@Composable
private fun SectionLabel(text: Int) {
    Text(
        text = stringResource(text),
        color = Muted,
        fontSize = 16.sp,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 4.dp),
    )
}

/** One option of a group: white when chosen, grey otherwise. */
@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit, fontFamily: FontFamily? = null) {
    Text(
        text = label,
        fontSize = 22.sp,
        fontFamily = fontFamily,
        color = if (selected) Color.White else Muted,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

@Composable
private fun Hint(text: Int, onClick: (() -> Unit)? = null) {
    Text(
        text = stringResource(text),
        color = Muted,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 24.dp, vertical = 6.dp),
    )
}
