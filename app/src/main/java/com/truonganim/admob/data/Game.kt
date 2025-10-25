package com.truonganim.admob.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import org.json.JSONArray
import org.json.JSONObject

/**
 * Game Result
 */
enum class GameResult {
    WIN,
    LOSE,
    CANCELLED
}

/**
 * Game Data Model
 */
data class Game(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailUrl: String,
    val adsRequired: Int = 0, // Number of ads required to unlock
    val inputImages: List<String> = emptyList(), // Images used in the game
    val activityClass: Class<out Activity>, // Activity to start
    val isUnlocked: Boolean = false
) {
    companion object {
        // Request code for game activity result
        const val GAME_RESULT_CODE = 1001

        // Intent extras keys
        const val EXTRA_GAME_ID = "extra_game_id"
        const val EXTRA_INPUT_IMAGES = "extra_input_images"
        const val EXTRA_GAME_RESULT = "extra_game_result"

        /**
         * Create intent to start game
         */
        fun createIntent(context: Context, game: Game): Intent {
            return Intent(context, game.activityClass).apply {
                putExtra(EXTRA_GAME_ID, game.id)
                putStringArrayListExtra(EXTRA_INPUT_IMAGES, ArrayList(game.inputImages))
            }
        }

        /**
         * Parse game result from intent
         */
        fun parseResult(intent: Intent?): GameResult {
            return intent?.getSerializableExtra(EXTRA_GAME_RESULT) as? GameResult
                ?: GameResult.CANCELLED
        }

        /**
         * Map activity type string to Activity class
         */
        private fun getActivityClass(activityType: String): Class<out Activity> {
            return when (activityType) {
//                "tap_to_zoom" -> com.truonganim.admob.games.taptozoom.TapToZoomGameActivity::class.java
                // Add more game types here as they are created
                else -> com.truonganim.admob.games.imagepuzzle.ImagePuzzleGameActivity::class.java // Default
            }
        }

        /**
         * Parse Game from JSON object
         */
        fun fromJson(json: JSONObject): Game {
            val inputImagesArray = json.optJSONArray("input_images")
            val inputImages = mutableListOf<String>()
            if (inputImagesArray != null) {
                for (i in 0 until inputImagesArray.length()) {
                    inputImages.add(inputImagesArray.getString(i))
                }
            }

            val activityType = json.getString("activity_type")

            return Game(
                id = json.getString("id"),
                name = json.getString("name"),
                description = json.getString("description"),
                thumbnailUrl = json.getString("thumbnail_url"),
                adsRequired = json.optInt("ads_required", 0),
                inputImages = inputImages,
                activityClass = getActivityClass(activityType),
                isUnlocked = json.optBoolean("is_unlocked", false)
            )
        }

        /**
         * Parse list of Games from JSON array string
         */
        fun fromJsonArray(jsonString: String): List<Game> {
            return try {
                val jsonArray = JSONArray(jsonString)
                val games = mutableListOf<Game>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    games.add(fromJson(jsonObject))
                }
                games
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}



