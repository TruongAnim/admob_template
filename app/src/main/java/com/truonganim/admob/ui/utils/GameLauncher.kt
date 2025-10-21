package com.truonganim.admob.ui.utils

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameResult

/**
 * Game Launcher Helper
 * Provides a composable way to launch games and handle results
 */
@Composable
fun rememberGameLauncher(
    onGameWin: (String) -> Unit = {},
    onGameLose: (String) -> Unit = {},
    onGameCancelled: (String) -> Unit = {}
): GameLauncher {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val gameResult = result.data?.getSerializableExtra(Game.EXTRA_GAME_RESULT) as? GameResult
            val gameId = result.data?.getStringExtra(Game.EXTRA_GAME_ID) ?: ""
            when (gameResult) {
                GameResult.WIN -> onGameWin(gameId)
                GameResult.LOSE -> onGameLose(gameId)
                GameResult.CANCELLED -> onGameCancelled(gameId)
                null -> {}
            }
        }
    }
    
    return remember {
        GameLauncher(context, launcher)
    }
}

/**
 * Game Launcher class
 */
class GameLauncher(
    private val context: android.content.Context,
    private val launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    /**
     * Launch a game
     */
    fun launch(game: Game) {
        val intent = Game.createIntent(context, game)
        launcher.launch(intent)
    }
}

