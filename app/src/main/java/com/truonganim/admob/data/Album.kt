package com.truonganim.admob.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Album Data
 */
data class Album(
    val id: String,
    val name: String,  // Display name (e.g., "FREE", "LIMITED", "FORBIDDEN", "ALL")
    val albumId: String,  // Used to map with characters
    val thumbnail: String,
    val requiredAdCount: Int = 0,  // Number of ads required to unlock
    val watchedAdCount: Int = 0,   // Number of ads user has watched
    val order: Int = 0
) {
    /**
     * Check if album is unlocked
     */
    val isUnlocked: Boolean
        get() = watchedAdCount >= requiredAdCount

    /**
     * Get progress text (e.g., "0/1", "1/2")
     */
    val progressText: String
        get() = "$watchedAdCount/$requiredAdCount"

    /**
     * Get remaining ads to unlock
     */
    val remainingAds: Int
        get() = maxOf(0, requiredAdCount - watchedAdCount)

    companion object {
        /**
         * Parse Album from JSON object
         */
        fun fromJson(json: JSONObject): Album {
            return Album(
                id = json.getString("id"),
                name = json.getString("name"),
                albumId = json.getString("album_id"),
                thumbnail = json.getString("thumbnail"),
                requiredAdCount = json.optInt("required_ad_count", 0),
                watchedAdCount = json.optInt("watched_ad_count", 0),
                order = json.optInt("order", 0)
            )
        }

        /**
         * Parse list of Albums from JSON array string
         */
        fun fromJsonArray(jsonString: String): List<Album> {
            return try {
                val jsonArray = JSONArray(jsonString)
                val albums = mutableListOf<Album>()
                for (i in 0 until jsonArray.length()) {
                    val albumJson = jsonArray.getJSONObject(i)
                    albums.add(fromJson(albumJson))
                }
                albums.sortedBy { it.order }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

