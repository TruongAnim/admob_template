package com.truonganim.admob.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.ui.albums.AlbumsScreen
import com.truonganim.admob.ui.favorites.FavoritesScreen
import com.truonganim.admob.ui.settings.SettingsScreen

/**
 * Home Screen with Bottom Navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAlbumClick: (AlbumCategory) -> Unit = {},
    onCharacterClick: (Int) -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onFavouritePhotoClick: (String, List<String>) -> Unit = { _, _ -> }
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Albums.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Albums.route) {
                AlbumsScreen(
                    onAlbumClick = onAlbumClick
                )
            }
            composable(BottomNavItem.Favorites.route) {
                FavoritesScreen(
                    onCharacterClick = onCharacterClick,
                    onPhotoClick = onFavouritePhotoClick,
                    onViewAllCharactersClick = {
                        onAlbumClick(AlbumCategory.FAVOURITE)
                    },
                    onViewAllPhotosClick = {
                        onCharacterClick(AppCharacter.FAVOURITE_PHOTOS_ID)
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

