package com.tokvison.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// TV apps are almost always viewed dark-room / dark-first, and a single color scheme
// keeps this module tiny (no light theme, no dynamic color, no extra assets to ship).
private val TokVisonColorScheme = darkColorScheme(
    background = TokVisonBackground,
    surface = TokVisonSurface,
    surfaceVariant = TokVisonSurfaceVariant,
    primary = TokVisonAccent,
    onBackground = TokVisonOnBackground,
    onSurface = TokVisonOnBackground,
)

@Composable
fun TokVisonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TokVisonColorScheme,
        typography = TokVisonTypography,
        content = content,
    )
}
