package io.github.winter000boy.minimalist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

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

private val typography = Typography().run {
    val serif = FontFamily.Serif
    Typography(
        displayLarge = displayLarge.copy(fontFamily = serif),
        displayMedium = displayMedium.copy(fontFamily = serif),
        displaySmall = displaySmall.copy(fontFamily = serif),
        headlineLarge = headlineLarge.copy(fontFamily = serif),
        headlineMedium = headlineMedium.copy(fontFamily = serif),
        headlineSmall = headlineSmall.copy(fontFamily = serif),
        titleLarge = titleLarge.copy(fontFamily = serif),
        titleMedium = titleMedium.copy(fontFamily = serif),
        titleSmall = titleSmall.copy(fontFamily = serif),
        bodyLarge = bodyLarge.copy(fontFamily = serif),
        bodyMedium = bodyMedium.copy(fontFamily = serif),
        bodySmall = bodySmall.copy(fontFamily = serif),
        labelLarge = labelLarge.copy(fontFamily = serif),
        labelMedium = labelMedium.copy(fontFamily = serif),
        labelSmall = labelSmall.copy(fontFamily = serif),
    )
}

/** Pure black, white serif text. */
@Composable
fun MinimalistTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            contentColor = Color.White,
            content = content,
        )
    }
}
