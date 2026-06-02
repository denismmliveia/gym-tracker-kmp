package com.gymtracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val SportTypography = Typography(
    titleLarge  = TextStyle(fontWeight = FontWeight.Black,     letterSpacing = 0.03.em, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Black,     letterSpacing = 0.03.em, fontSize = 16.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.02.em, fontSize = 14.sp),
    bodyLarge   = TextStyle(fontWeight = FontWeight.Normal,    letterSpacing = 0.01.em, fontSize = 16.sp),
    bodyMedium  = TextStyle(fontWeight = FontWeight.Normal,    letterSpacing = 0.01.em, fontSize = 14.sp),
    bodySmall   = TextStyle(fontWeight = FontWeight.Normal,    letterSpacing = 0.02.em, fontSize = 12.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Bold,      letterSpacing = 0.08.em, fontSize = 11.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Bold,      letterSpacing = 0.06.em, fontSize = 12.sp),
)

private val SportShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small      = RoundedCornerShape(4.dp),
    medium     = RoundedCornerShape(6.dp),
    large      = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GymTrackerTheme(
    primaryColor: Color = Color(0xFFFF6D00),
    secondaryColor: Color = Color(0xFFFF3D00),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary          = primaryColor,
        onPrimary        = Color(0xFF000000),
        secondary        = secondaryColor,
        onSecondary      = Color(0xFF000000),
        background       = Color(0xFF0A0A0A),
        surface          = Color(0xFF161616),
        surfaceVariant   = Color(0xFF1E1E1E),
        onBackground     = Color.White,
        onSurface        = Color.White,
        onSurfaceVariant = Color.White
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SportTypography,
        shapes      = SportShapes,
        content     = content
    )
}
