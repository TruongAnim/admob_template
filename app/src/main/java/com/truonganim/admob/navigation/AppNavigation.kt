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
import com.truonganim.admob.ui.home.HomeScreen

/**
 * Navigation Routes
 */
object Routes {
    const val HOME = "home"
    const val ALBUM_DETAIL = "album_detail/{albumCategory}"
    
    fun albumDetail(albumCategory: AlbumCategory): String {
        return "album_detail/${albumCategory.name}"
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
                }
            )
        }
    }
}

