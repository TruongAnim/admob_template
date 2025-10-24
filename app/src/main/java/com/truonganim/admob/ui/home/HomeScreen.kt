package com.truonganim.admob.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.truonganim.admob.ads.InterstitialAdManager
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.Game
import com.truonganim.admob.ui.albums.AlbumsScreen
import com.truonganim.admob.ui.favorites.FavoritesScreen
import com.truonganim.admob.ui.games.GamesScreen
import com.truonganim.admob.ui.settings.SettingsScreen
import com.truonganim.admob.ui.theme.LocalAppColors

/**
 * Home Screen with Bottom Navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAlbumClick: (String) -> Unit = {}, // Now takes albumId instead of AlbumCategory
    onCharacterClick: (Int) -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onFavouritePhotoClick: (String, List<String>) -> Unit = { _, _ -> },
    onGameClick: (Game) -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Start ad load timer when entering home screen
    LaunchedEffect(Unit) {
        val adManager = InterstitialAdManager.getInstance(context)
        adManager.startAdLoadTimer()
    }

    // Stop timer when leaving home screen
    DisposableEffect(Unit) {
        onDispose {
            // Don't stop timer - let it run in background
        }
    }

    // Get current route to determine which tab is selected
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Find current tab item
    val currentTab = BottomNavItem.items.find { it.route == currentRoute } ?: BottomNavItem.Albums

    val appColors = LocalAppColors.current

    // Determine background color based on current tab
    // Settings tab uses normal background, other tabs use image screen background
    val barBackgroundColor = if (currentTab == BottomNavItem.Settings) {
        MaterialTheme.colorScheme.background
    } else {
        appColors.imageScreenBackground
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = stringResource(currentTab.titleWithEmojiResId),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barBackgroundColor,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = barBackgroundColor
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleResId)
                            )
                        },
                        label = { Text(stringResource(item.titleResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.background
                        )
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
                        onAlbumClick("favourite") // Use albumId string
                    },
                    onViewAllPhotosClick = {
                        onCharacterClick(AppCharacter.FAVOURITE_PHOTOS_ID)
                    },
                    onExploreClick = {
                        // Navigate to Albums tab
                        navController.navigate(BottomNavItem.Albums.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Games.route) {
                GamesScreen(
                    onGameClick = onGameClick,
                    onCharacterClick = onCharacterClick,
                    onViewAllCharactersClick = {
                        // Navigate to game album detail
                        onAlbumClick(com.truonganim.admob.config.AppConfig.GAME.GAME_ALBUM_ID)
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                val context = LocalContext.current
                SettingsScreen(
                    onLanguageClick = {
                        // Start LanguageActivity from Settings
                        val intent = com.truonganim.admob.ui.language.LanguageActivity.createIntent(
                            context = context,
                            fromSettings = true
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

