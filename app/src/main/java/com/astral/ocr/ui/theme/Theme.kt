package com.astral.ocr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AuroraCyan,
    secondary = StellarPink,
    tertiary = CosmicPurple
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicPurple,
    secondary = StellarPink,
    tertiary = AuroraCyan
)

@Composable
fun AstralOCRTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

fun Modifier.gradientBackground(): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                MidnightBlue,
                CosmicPurple,
                StellarPink.copy(alpha = 0.8f)
            )
        ),
        size = size
    )
}
