package com.truonganim.admob.data

import android.content.Context
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing Game data
 */
class GameRepository private constructor(private val context: Context) {

    private val remoteConfigHelper = RemoteConfigHelper.getInstance()
    private val preferencesManager = PreferencesManager.getInstance(context)

    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games.asStateFlow()

    /**
     * Load games from Remote Config
     */
    suspend fun loadGames() {
        val jsonString = remoteConfigHelper.getString(RemoteConfigKeys.GAMES_DATA)
        val gameList = Game.fromJsonArray(jsonString)
        _games.value = gameList
    }

    /**
     * Get game by ID
     */
    fun getGameById(id: String): Game? {
        return _games.value.find { it.id == id }
    }

    /**
     * Unlock game
     */
    fun unlockGame(gameId: String) {
        _games.value = _games.value.map { game ->
            if (game.id == gameId) {
                game.copy(isUnlocked = true)
            } else {
                game
            }
        }
    }

    companion object {
        @Volatile
        private var instance: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return instance ?: synchronized(this) {
                instance ?: GameRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

