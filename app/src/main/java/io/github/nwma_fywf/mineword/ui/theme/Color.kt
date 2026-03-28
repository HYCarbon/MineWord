package io.github.nwma_fywf.mineword.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

data class ThemeColor(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

val StandardThemeColors = listOf(
    ThemeColor(
        name = "紫",
        primary = Color(0xFF6650a4),
        secondary = Color(0xFF625b71),
        tertiary = Color(0xFF7D5260)
    ),
    ThemeColor(
        name = "蓝",
        primary = Color(0xFF1976D2),
        secondary = Color(0xFF455A64),
        tertiary = Color(0xFF00796B)
    ),
    ThemeColor(
        name = "青",
        primary = Color(0xFF00838F),
        secondary = Color(0xFF4DB6AC),
        tertiary = Color(0xFFFFC107)
    ),
    ThemeColor(
        name = "绿",
        primary = Color(0xFF388E3C),
        secondary = Color(0xFF5D4037),
        tertiary = Color(0xFFF57C00)
    ),
    ThemeColor(
        name = "橙",
        primary = Color(0xFFF57C00),
        secondary = Color(0xFF5D4037),
        tertiary = Color(0xFF388E3C)
    ),
    ThemeColor(
        name = "红",
        primary = Color(0xFFD32F2F),
        secondary = Color(0xFF5D4037),
        tertiary = Color(0xFF7B1FA2)
    ),
    ThemeColor(
        name = "粉",
        primary = Color(0xFFC2185B),
        secondary = Color(0xFF5D4037),
        tertiary = Color(0xFF1976D2)
    ),
    ThemeColor(
        name = "棕",
        primary = Color(0xFF5D4037),
        secondary = Color(0xFF795548),
        tertiary = Color(0xFF4DB6AC)
    ),
    ThemeColor(
        name = "灰",
        primary = Color(0xFF616161),
        secondary = Color(0xFF757575),
        tertiary = Color(0xFF9E9E9E)
    )
)
