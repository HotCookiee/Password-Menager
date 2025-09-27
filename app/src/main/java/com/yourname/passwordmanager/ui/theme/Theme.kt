package com.yourname.passwordmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme for the password manager
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary60,       // бирюзовый акцент
    secondary = Secondary60,   // фиолетовый акцент
    tertiary = Indigo,         // дополнительный акцент
    background = Grey900,
    surface = Grey800,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Grey100,
    onSurface = Grey100,
)

/**
 * Light color scheme for the password manager
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary40,       // тёмный бирюзовый
    secondary = Secondary40,   // глубокий фиолетовый
    tertiary = Cyan,           // дополнительный акцент
    background = Grey50,
    surface = Grey100,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Grey900,
    onSurface = Grey900,
)

/**
 * Main theme composable for the Password Manager app
 */
@Composable
fun PasswordManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
