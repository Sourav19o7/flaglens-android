package dev.local.androidtools.flaglens.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(primary = Color(0xFFB39DDB), secondary = Color(0xFF7C4DFF))
private val LightColors = lightColorScheme(primary = Color(0xFF512DA8), secondary = Color(0xFF7C4DFF))

@Composable
fun FlagLensSampleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
