package com.example.cargoconnectapp.ui.models.helpermodels

object PackageColorPool {
    val colors = listOf(
        "Red"           to 0xFFE53935,
        "Blue"          to 0xFF1E88E5,
        "Green"         to 0xFF43A047,
        "Yellow"        to 0xFFFDD835,
        "Orange"        to 0xFFFB8C00,
        "Purple"        to 0xFF8E24AA,
        "Pink"          to 0xFFE91E63,
        "Cyan"          to 0xFF00ACC1,
        "Magenta"       to 0xFFD81B60,
        "Lime"          to 0xFF7CB342,
        "Indigo"        to 0xFF3949AB,
        "Violet"        to 0xFF5E35B1,
        "Teal"          to 0xFF00897B,
        "Coral"         to 0xFFFF7043,
        "Salmon"        to 0xFFEF5350,
        "Crimson"       to 0xFFC62828,
        "Scarlet"       to 0xFFB71C1C,
        "Ruby"          to 0xFFAD1457,
        "Maroon"        to 0xFF880E4F,
        "Rose"          to 0xFFF48FB1,
        "Fuchsia"       to 0xFFAA00FF,
        "Lavender"      to 0xFF9FA8DA,
        "Periwinkle"    to 0xFF7986CB,
        "Cobalt"        to 0xFF1565C0,
        "Navy"          to 0xFF0D47A1,
        "Sky Blue"      to 0xFF29B6F6,
        "Turquoise"     to 0xFF00BCD4,
        "Mint"          to 0xFF80CBC4,
        "Emerald"       to 0xFF2E7D32,
        "Olive"         to 0xFF827717,
        "Forest Green"  to 0xFF1B5E20,
        "Sage"          to 0xFF558B2F,
        "Chartreuse"    to 0xFFCDDC39,
        "Amber"         to 0xFFFFB300,
        "Gold"          to 0xFFFFD600,
        "Mustard"       to 0xFFF9A825,
        "Tangerine"     to 0xFFFF6D00,
        "Peach"         to 0xFFFFAB91,
        "Apricot"       to 0xFFFFCC80,
        "Bronze"        to 0xFFBF360C,
        "Copper"        to 0xFFD84315,
        "Chocolate"     to 0xFF4E342E,
        "Sienna"        to 0xFF6D4C41,
        "Mahogany"      to 0xFF3E2723,
        "Burgundy"      to 0xFF4A148C,
        "Plum"          to 0xFF6A1B9A,
        "Eggplant"      to 0xFF4A148C,
        "Lilac"         to 0xFFCE93D8,
        "Mauve"         to 0xFFBA68C8,
        "Taupe"         to 0xFF78909C
    )

    fun getColor(colorName: String?): androidx.compose.ui.graphics.Color {
        val hex = colors.firstOrNull { it.first.equals(colorName, ignoreCase = true) }?.second
            ?: 0xFF9E9E9E // default grey if not found
        return androidx.compose.ui.graphics.Color(hex)
    }

    fun getColorOrNull(colorName: String?): androidx.compose.ui.graphics.Color? {
        val hex = colors.firstOrNull { it.first.equals(colorName, ignoreCase = true) }?.second
            ?: return null
        return androidx.compose.ui.graphics.Color(hex)
    }
}