package com.truonganim.admob.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom Navigation Items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val titleWithEmoji: String,
    val icon: ImageVector
) {
    object Albums : BottomNavItem(
        route = "albums",
        title = "Albums",
        titleWithEmoji = "🏠 Categories",
        icon = Icons.Default.Home
    )

    object Favorites : BottomNavItem(
        route = "favorites",
        title = "Favorites",
        titleWithEmoji = "🔥 Favorites",
        icon = Icons.Default.Favorite
    )

    object Games : BottomNavItem(
        route = "games",
        title = "Games",
        titleWithEmoji = "🎮 Games",
        icon = Icons.Default.SportsEsports
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "Settings",
        titleWithEmoji = "⚙️ Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Albums, Favorites, Games, Settings)
    }
}

