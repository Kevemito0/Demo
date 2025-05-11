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



private val LightColorScheme = lightColorScheme(
    primary        = Color(0xFF000000),
    onPrimary      = Color.White,
    primaryContainer   = Color(0xFFDDE4FF),
    onPrimaryContainer = Color(0xFF173B69),

    secondary      = Color(0x97A4B8FD),
    onSecondary    = Color.Black,
    secondaryContainer   = Color(0xFFAAFFF9),
    onSecondaryContainer = Color(0xFF00201A),

    background     = Color(0xFFEFEFEF),
    onBackground   = Color.Black,
    surface        = Color.White,
    onSurface      = Color.Black,
    surfaceVariant = Color(0x1B757575),

    error          = Color(0xFFB00020),
    onError        = Color.White,
    errorContainer    = Color(0xFFFFDAD4),
    onErrorContainer  = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary        = Color(0xFFFFFFFF),
    onPrimary      = Color(0xFFEFEFEF),
    primaryContainer   = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary      = Color(0xFF3E3E6E),
    onSecondary    = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFF00504D),
    onSecondaryContainer = Color(0xE197E1EC),

    background     = Color(0xFF1A1A40),
    onBackground   = Color.White,
    surface        = Color(0xFF121212),
    onSurface      = Color.White,
    surfaceVariant = Color(0x37757575),


    error          = Color(0xFFCF6679),
    onError        = Color.Black,
    errorContainer    = Color(0xFF93000A),
    onErrorContainer  = Color(0xFFFFDAD4)
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
    labelSmall    = TextStyle(fontSize = 11.sp),
    labelLarge    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    // …etc.
)

@Composable
fun CapstoneTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
    dynamicColor: Boolean = false,
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