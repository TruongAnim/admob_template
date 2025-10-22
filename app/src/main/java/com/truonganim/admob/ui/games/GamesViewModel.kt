package com.truonganim.admob.ui.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.ads.RewardAdHelper
import com.truonganim.admob.ads.RewardAdPlace
import com.truonganim.admob.config.AppConfig
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameRepository
import com.truonganim.admob.data.filterByAlbumId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Games Screen UI State
 */
data class GamesUiState(
    val games: List<Game> = emptyList(),
    val gameAlbumCharacters: List<AppCharacter> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Games ViewModel
 */
class GamesViewModel(private val context: Context) : ViewModel() {

    private val gameRepository = GameRepository.getInstance(context)
    private val characterRepository = CharacterRepository.getInstance(context)
    private val _isLoading = MutableStateFlow(false)
    private val _pendingUnlockCharacterId = MutableStateFlow<Int?>(null)
    private val _pendingGameUnlockCharacterId = MutableStateFlow<Int?>(null) // Character waiting for game win

    // Observe games and characters from repositories
    val uiState: StateFlow<GamesUiState> = combine(
        gameRepository.games,
        characterRepository.characters,
        _isLoading
    ) { games, characters, isLoading ->
        // Filter characters from game album_detail
        val gameAlbumCharacters = characters.filterByAlbumId(AppConfig.GAME.GAME_ALBUM_ID)
        GamesUiState(
            games = games,
            gameAlbumCharacters = gameAlbumCharacters,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamesUiState()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                gameRepository.loadGames()
                characterRepository.loadCharacters()
            } catch (e: Exception) {
                // Handle error if needed
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle favorite for a character
     */
    fun onCharacterFavoriteClick(character: AppCharacter) {
        viewModelScope.launch {
            characterRepository.toggleFavorite(character.id)
        }
    }

    /**
     * Handle character click - show game or ad based on lock type
     * Returns: null if unlocked (navigate to character detail)
     *          Game if locked by game (launch game)
     *          "ad" if locked by ad (show reward ad)
     */
    fun onCharacterClick(appCharacter: AppCharacter): Any? {
        return when {
            appCharacter.isUnlocked -> null // Navigate to character detail
            appCharacter.isLockedByGame -> {
                // Save character ID for unlock after game win
                _pendingGameUnlockCharacterId.value = appCharacter.id
                // Find and return the game
                appCharacter.lockedByGame?.let { gameId ->
                    gameRepository.getGameById(gameId)
                }
            }
            appCharacter.isLockedByAd -> {
                // Return "ad" to indicate should show ad
                "ad"
            }
            else -> null
        }
    }

    /**
     * Show reward ad to unlock character
     */
    fun showRewardAdToUnlock(appCharacter: AppCharacter) {
        _pendingUnlockCharacterId.value = appCharacter.id

        RewardAdHelper.requestRewardAd(
            context = context,
            place = RewardAdPlace.UNLOCK_CHARACTER,
            onRewardEarned = { amount ->
                viewModelScope.launch {
                    _pendingUnlockCharacterId.value?.let { characterId ->
                        val unlocked = characterRepository.unlockCharacterByAd(characterId)
                        if (unlocked) {
                            println("✅ Character $characterId unlocked by ad!")
                        } else {
                            println("📺 Ad progress incremented for character $characterId")
                        }
                    }
                    _pendingUnlockCharacterId.value = null
                }
            },
            onAdClosed = {
                _pendingUnlockCharacterId.value = null
            }
        )
    }

    /**
     * Unlock character after winning game
     * Uses the pending character ID that was saved when launching the game
     */
    fun unlockCharacterByGameWin() {
        viewModelScope.launch {
            _pendingGameUnlockCharacterId.value?.let { characterId ->
                characterRepository.unlockCharacterByGame(characterId)
                println("🎮 Character $characterId unlocked by game win!")
            }
            _pendingGameUnlockCharacterId.value = null
        }
    }

    /**
     * Clear pending game unlock (when game is cancelled or lost)
     */
    fun clearPendingGameUnlock() {
        _pendingGameUnlockCharacterId.value = null
    }
}

