package com.truonganim.admob.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.truonganim.admob.R
import com.truonganim.admob.ads.InterstitialAdManager
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.Game
import com.truonganim.admob.ui.albums.AlbumsScreen
import com.truonganim.admob.ui.favorites.FavoritesScreen
import com.truonganim.admob.ui.games.GamesScreen
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
    onFavouritePhotoClick: (String, List<String>) -> Unit = { _, _ -> },
    onGameClick: (Game) -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Load interstitial ad when entering home screen
    LaunchedEffect(Unit) {
        val adManager = InterstitialAdManager.getInstance(context)
        adManager.loadAd()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_category_title),
                            contentDescription = "null",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            text = "Category",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
            )
        },
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
            composable(BottomNavItem.Games.route) {
                GamesScreen(
                    onGameClick = onGameClick
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

