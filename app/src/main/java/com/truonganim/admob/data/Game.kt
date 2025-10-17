package com.truonganim.admob.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

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
    }
}

/**
 * Sample Games Data
 */
object SampleGames {
    fun getGames(): List<Game> {
        return listOf(
            Game(
                id = "tap_to_zoom",
                name = "Tap to Zoom",
                description = "Tap the image to zoom in and win!",
                thumbnailUrl = "https://picsum.photos/400/200?random=1",
                adsRequired = 0,
                inputImages = listOf("https://picsum.photos/800/600?random=100"),
                activityClass = com.truonganim.admob.games.taptozoom.TapToZoomGameActivity::class.java,
                isUnlocked = true
            )
            // More games will be added here
        )
    }
}

