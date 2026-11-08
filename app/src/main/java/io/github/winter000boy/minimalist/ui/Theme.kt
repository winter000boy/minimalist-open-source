package io.github.winter000boy.minimalist.ui

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import io.github.winter000boy.minimalist.data.AppFont
import io.github.winter000boy.minimalist.data.TextSize

/** Secondary text: hints, section labels, the inactive A–Z letters. */
val Muted = Color(0xFF8A8A8A)

private val colors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceContainer = Color(0xFF1C1C1C),
    onSurfaceVariant = Muted,
    outline = Color.White,
)

/** System fonts only, so every choice works on every phone without bundling font files. */
fun AppFont.fontFamily(): FontFamily = when (this) {
    AppFont.Serif -> FontFamily.Serif
    AppFont.Sans -> FontFamily.SansSerif
    AppFont.Light -> FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL))
    AppFont.Condensed -> FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    AppFont.Mono -> FontFamily.Monospace
}

private fun typographyIn(family: FontFamily) = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = family),
        displayMedium = displayMedium.copy(fontFamily = family),
        displaySmall = displaySmall.copy(fontFamily = family),
        headlineLarge = headlineLarge.copy(fontFamily = family),
        headlineMedium = headlineMedium.copy(fontFamily = family),
        headlineSmall = headlineSmall.copy(fontFamily = family),
        titleLarge = titleLarge.copy(fontFamily = family),
        titleMedium = titleMedium.copy(fontFamily = family),
        titleSmall = titleSmall.copy(fontFamily = family),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family),
        labelMedium = labelMedium.copy(fontFamily = family),
        labelSmall = labelSmall.copy(fontFamily = family),
    )
}

/** Pure black, white text in the user's font, every size scaled by their text size. */
@Composable
fun MinimalistTheme(
    font: AppFont = AppFont.Serif,
    textSize: TextSize = TextSize.Default,
    content: @Composable () -> Unit,
) {
    val typography = remember(font) { typographyIn(font.fontFamily()) }
    val density = LocalDensity.current
    // Scaling the density's font scale resizes every sp in the app at once. Left untouched at the
    // default size, so Android 14's non-linear scaling of very large system fonts still applies.
    val scaledDensity = remember(density, textSize) {
        if (textSize == TextSize.Default) density else Density(density.density, density.fontScale * textSize.scale)
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(colorScheme = colors, typography = typography) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black,
                contentColor = Color.White,
                content = content,
            )
        }
    }
}
