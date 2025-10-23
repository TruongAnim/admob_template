package com.truonganim.admob.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.truonganim.admob.R

/**
 * Bottom Navigation Items
 */
sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    @StringRes val titleWithEmojiResId: Int,
    val icon: ImageVector
) {
    object Albums : BottomNavItem(
        route = "albums",
        titleResId = R.string.nav_albums,
        titleWithEmojiResId = R.string.nav_albums_emoji,
        icon = Icons.Default.Home
    )

    object Favorites : BottomNavItem(
        route = "favorites",
        titleResId = R.string.nav_favorites,
        titleWithEmojiResId = R.string.nav_favorites_emoji,
        icon = Icons.Default.Favorite
    )

    object Games : BottomNavItem(
        route = "games",
        titleResId = R.string.nav_games,
        titleWithEmojiResId = R.string.nav_games_emoji,
        icon = Icons.Default.SportsEsports
    )

    object Settings : BottomNavItem(
        route = "settings",
        titleResId = R.string.nav_settings,
        titleWithEmojiResId = R.string.nav_settings_emoji,
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Albums, Favorites, Games, Settings)
    }
}

