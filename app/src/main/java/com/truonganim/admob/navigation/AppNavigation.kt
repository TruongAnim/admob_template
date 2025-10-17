package com.truonganim.admob.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.ui.albumdetail.AlbumDetailScreen
import com.truonganim.admob.ui.characterdetail.CharacterDetailScreen
import com.truonganim.admob.ui.home.HomeScreen
import com.truonganim.admob.ui.photoviewer.PhotoViewerScreen

/**
 * Navigation Routes
 */
object Routes {
    const val HOME = "home"
    const val ALBUM_DETAIL = "album_detail/{albumCategory}"
    const val CHARACTER_DETAIL = "character_detail/{characterId}"
    const val PHOTO_VIEWER = "photo_viewer/{characterId}/{photoIndex}"

    fun albumDetail(albumCategory: AlbumCategory): String {
        return "album_detail/${albumCategory.name}"
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
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Home Screen with Bottom Navigation
        composable(Routes.HOME) {
            HomeScreen(
                onAlbumClick = { albumCategory ->
                    navController.navigate(Routes.albumDetail(albumCategory))
                },
                onCharacterClick = { characterId ->
                    navController.navigate(Routes.characterDetail(characterId))
                },
                onPhotoClick = { photoUrl ->
                    // TODO: Handle photo click from favourites
                    // For now, we don't have a direct route to photo viewer from URL
                    // This will be handled later
                }
            )
        }
        
        // Album Detail Screen
        composable(
            route = Routes.ALBUM_DETAIL,
            arguments = listOf(
                navArgument("albumCategory") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val albumCategoryName = backStackEntry.arguments?.getString("albumCategory")
            val albumCategory = AlbumCategory.valueOf(albumCategoryName ?: AlbumCategory.NORMAL.name)

            AlbumDetailScreen(
                albumCategory = albumCategory,
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
}

