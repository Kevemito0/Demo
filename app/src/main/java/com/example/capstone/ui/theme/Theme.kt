package com.example.capstone.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Shapes as MaterialShapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

private val LightColorScheme = lightColorScheme(
    primary        = Color(0xFF6750A4),
    onPrimary      = Color.White,
    secondary      = Color(0xFF625B71),
    onSecondary    = Color.White,
    background     = Color(0xFFFFFBFE),
    onBackground  = Color(0xFF1C1B1F),
    surface        = Color(0xFFFFFBFE),
    onSurface      = Color(0xFF1C1B1F),
    // …other roles: error, outline, etc.
)

private val DarkColorScheme = darkColorScheme(
    primary        = Color(0xFFD0BCFF),
    onPrimary      = Color(0xFF381E72),
    secondary      = Color(0xFFEEA5BA),
    onSecondary    = Color(0xFF492532),
    background     = Color(0xFF1A1A40),
    onBackground  = Color(0xFF1F1E1F),
    surface        = Color(0xFF1C1B1F),
    onSurface      = Color(0xFFE6E1E5),
    // …other roles
)

val AppShapes: MaterialShapes = MaterialShapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large  = RoundedCornerShape(16.dp)
)


val AppTypography: Typography = Typography(
    displayLarge  = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 24.sp),
    bodyMedium    = TextStyle(fontSize = 16.sp),
    labelSmall    = TextStyle(fontSize = 11.sp)
    // …etc.
)

@Composable
fun CapstoneTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}

@Composable
fun CapstoneTesting(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}