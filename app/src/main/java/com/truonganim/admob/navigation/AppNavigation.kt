package com.truonganim.admob.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truonganim.admob.ads.RewardAdManager
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.Game
import com.truonganim.admob.ui.albumdetail.AlbumDetailScreen
import com.truonganim.admob.ui.characterdetail.CharacterDetailScreen
import com.truonganim.admob.ui.components.AppLoadingOverlay
import com.truonganim.admob.ui.components.AppLoadingOverlayManager
import com.truonganim.admob.ui.components.RewardAdLoadingOverlay
import com.truonganim.admob.ui.home.HomeScreen
import com.truonganim.admob.ui.photoviewer.PhotoViewerScreen

/**
 * Navigation Routes
 */
object Routes {
    const val HOME = "home"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val CHARACTER_DETAIL = "character_detail/{characterId}"
    const val PHOTO_VIEWER = "photo_viewer/{characterId}/{photoIndex}"

    fun albumDetail(albumId: String): String {
        return "album_detail/$albumId"
    }

    fun characterDetail(characterId: Int): String {
        return "character_detail/$characterId"
    }

    fun photoViewer(characterId: Int, photoIndex: Int): String {
        return "photo_viewer/$characterId/$photoIndex"
    }
}

/**
 * App Navigation
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // Reward Ad Manager
    val rewardAdManager = RewardAdManager.getInstance(context)
    val rewardAdLoadingState by rewardAdManager.loadingState.collectAsState()

    // App Loading Overlay Manager
    val appLoadingManager = AppLoadingOverlayManager.getInstance()
    val appLoadingState by appLoadingManager.loadingState.collectAsState()

    Box {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
        // Home Screen with Bottom Navigation
        composable(Routes.HOME) {
            HomeScreen(
                onAlbumClick = { albumId ->
                    navController.navigate(Routes.albumDetail(albumId))
                },
                onCharacterClick = { characterId ->
                    navController.navigate(Routes.characterDetail(characterId))
                },
                onPhotoClick = { photoUrl ->
                    // Not used anymore - kept for compatibility
                },
                onFavouritePhotoClick = { photoUrl, allPhotos ->
                    // Find the index of the clicked photo in the list
                    val photoIndex = allPhotos.indexOf(photoUrl).coerceAtLeast(0)
                    // Navigate to PhotoViewer with FAVOURITE_PHOTOS_ID
                    navController.navigate(Routes.photoViewer(AppCharacter.FAVOURITE_PHOTOS_ID, photoIndex))
                },
                onGameClick = { game ->
                    // Start game activity
                    val intent = Game.createIntent(context, game)
                    context.startActivity(intent)
                }
            )
        }
        
        // Album Detail Screen
        composable(
            route = Routes.ALBUM_DETAIL,
            arguments = listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""

            AlbumDetailScreen(
                albumId = albumId,
                onBackClick = {
                    navController.popBackStack()
                },
                onCharacterClick = { characterId ->
                    navController.navigate(Routes.characterDetail(characterId))
                }
            )
        }

        // Character Detail Screen
        composable(
            route = Routes.CHARACTER_DETAIL,
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0

            CharacterDetailScreen(
                characterId = characterId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPhotoClick = { photoIndex ->
                    navController.navigate(Routes.photoViewer(characterId, photoIndex))
                }
            )
        }

        // Photo Viewer Screen
        composable(
            route = Routes.PHOTO_VIEWER,
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.IntType
                },
                navArgument("photoIndex") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0

            PhotoViewerScreen(
                characterId = characterId,
                initialPhotoIndex = photoIndex,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

        // Reward Ad Loading Overlay - covers entire app
        RewardAdLoadingOverlay(
            loadingState = rewardAdLoadingState,
            onDismiss = {
                rewardAdManager.cancelLoading()
            }
        )

        // App Loading Overlay - covers entire app
        AppLoadingOverlay(
            loadingState = appLoadingState,
            onCancel = {
                appLoadingManager.cancel()
            }
        )
    }
}

