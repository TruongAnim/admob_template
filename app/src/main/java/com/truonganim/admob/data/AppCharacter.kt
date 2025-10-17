package com.truonganim.admob.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Character data model
 */
data class AppCharacter(
    val id: Int,
    val name: String,
    val album: String,
    val order: Int,
    val adCount: Int,
    val thumbnail: String,
    val photos: List<String>,
    val isFavorite: Boolean = false,
    val isUnlocked: Boolean = false,
    val currentPhotoIndex: Int = 0
) {
    /**
     * Get progress text (e.g., "0/1", "1/2")
     */
    val progressText: String
        get() = "$currentPhotoIndex/${photos.size}"
    
    /**
     * Check if all photos are viewed
     */
    val isCompleted: Boolean
        get() = currentPhotoIndex >= photos.size
    
    companion object {
        /**
         * Special ID for favourite photos collection
         */
        const val FAVOURITE_PHOTOS_ID = -1

        /**
         * Parse Character from JSON object
         */
        fun fromJson(json: JSONObject): AppCharacter {
            val photosArray = json.getJSONArray("photos")
            val photos = mutableListOf<String>()
            for (i in 0 until photosArray.length()) {
                photos.add(photosArray.getString(i))
            }
            
            return AppCharacter(
                id = json.getInt("id"),
                name = json.getString("name"),
                album = json.getString("album"),
                order = json.getInt("order"),
                adCount = json.getInt("ad_count"),
                thumbnail = json.getString("thumbnail"),
                photos = photos,
                isFavorite = json.optBoolean("favourite", false),
                isUnlocked = json.optBoolean("is_unlocked", false),
                currentPhotoIndex = json.optInt("current_photo_index", 0)
            )
        }
        
        /**
         * Parse list of Characters from JSON array string
         */
        fun fromJsonArray(jsonString: String): List<AppCharacter> {
            return try {
                val jsonArray = JSONArray(jsonString)
                val appCharacters = mutableListOf<AppCharacter>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    appCharacters.add(fromJson(jsonObject))
                }
                appCharacters
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}

/**
 * Extension function to filter characters by album
 */
fun List<AppCharacter>.filterByAlbum(albumCategory: AlbumCategory): List<AppCharacter> {
    val albumName = when (albumCategory) {
        AlbumCategory.NORMAL -> "normal"
        AlbumCategory.ROLE_PLAY -> "role_play"
        AlbumCategory.HARD -> "hard"
        AlbumCategory.FULL -> "full"
        AlbumCategory.FAVOURITE -> return emptyList() // FAVOURITE is handled separately
    }
    return this.filter { it.album == albumName }
        .sortedBy { it.order }
}

