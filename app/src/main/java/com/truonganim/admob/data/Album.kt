package com.truonganim.admob.data

import androidx.annotation.DrawableRes

/**
 * Album Category
 */
enum class AlbumCategory(val displayName: String) {
    NORMAL("NORMAL"),
    ROLE_PLAY("ROLE PLAY"),
    HARD("H.A.R.D"),
    FULL("FULL"),
    FAVOURITE("FAVOURITES") // Special category for favourite characters
}

/**
 * Album Data
 */
data class Album(
    val id: String,
    val category: AlbumCategory,
    @DrawableRes val imageRes: Int,
    val currentCount: Int = 0,
    val totalCount: Int = 0
) {
    val progressText: String
        get() = "$currentCount/$totalCount"
}

/**
 * Sample Albums Data
 */
object SampleAlbums {
    fun getAlbums(): List<Album> {
        return listOf(
            Album(
                id = "normal_1",
                category = AlbumCategory.NORMAL,
                imageRes = android.R.drawable.ic_menu_gallery, // Placeholder
                currentCount = 0,
                totalCount = 1
            ),
            Album(
                id = "roleplay_1",
                category = AlbumCategory.ROLE_PLAY,
                imageRes = android.R.drawable.ic_menu_gallery, // Placeholder
                currentCount = 0,
                totalCount = 1
            ),
            Album(
                id = "hard_1",
                category = AlbumCategory.HARD,
                imageRes = android.R.drawable.ic_menu_gallery, // Placeholder
                currentCount = 0,
                totalCount = 2
            ),
            Album(
                id = "full_1",
                category = AlbumCategory.FULL,
                imageRes = android.R.drawable.ic_menu_gallery, // Placeholder
                currentCount = 0,
                totalCount = 3
            )
        )
    }
}

