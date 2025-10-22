package com.truonganim.admob.ui.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.ads.RewardAdHelper
import com.truonganim.admob.ads.RewardAdPlace
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.GameRepository
import com.truonganim.admob.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favorites Screen UI State
 */
data class FavoritesUiState(
    val favoriteAppCharacters: List<AppCharacter> = emptyList(),
    val favoritePhotos: List<String> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Favorites ViewModel
 */
class FavoritesViewModel(
    private val context: Context
) : ViewModel() {

    private val characterRepository = CharacterRepository.getInstance(context)
    private val photoRepository = PhotoRepository.getInstance(context)
    private val gameRepository = GameRepository.getInstance(context)

    private val _isLoading = MutableStateFlow(false)
    private val _pendingUnlockCharacterId = MutableStateFlow<Int?>(null)
    private val _pendingGameUnlockCharacterId = MutableStateFlow<Int?>(null)

    // Observe both characters and photos from repositories
    val uiState: StateFlow<FavoritesUiState> = combine(
        characterRepository.characters,
        photoRepository.favouritePhotoUrls,
        _isLoading
    ) { characters, favouritePhotoUrls, isLoading ->
        FavoritesUiState(
            favoriteAppCharacters = characters.filter { it.isFavorite },
            favoritePhotos = favouritePhotoUrls.toList(),
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load characters and favourites
                characterRepository.loadCharacters()
                photoRepository.loadFavourites()
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
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

    fun onPhotoClick(photoUrl: String) {
        // Navigation is handled by the screen composable
    }

    fun onCharacterFavoriteClick(appCharacter: AppCharacter) {
        viewModelScope.launch {
            characterRepository.toggleFavorite(appCharacter.id)
            // UI will auto-update via StateFlow
        }
    }

    fun onPhotoFavoriteClick(photoUrl: String) {
        viewModelScope.launch {
            photoRepository.toggleFavourite(photoUrl)
            // UI will auto-update via StateFlow
        }
    }
}

