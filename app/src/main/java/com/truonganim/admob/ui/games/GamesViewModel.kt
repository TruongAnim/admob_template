package com.truonganim.admob.ui.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.config.AppConfig
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameRepository
import com.truonganim.admob.data.filterByAlbumId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
class GamesViewModel(context: Context) : ViewModel() {

    private val gameRepository = GameRepository.getInstance(context)
    private val characterRepository = CharacterRepository.getInstance(context)
    private val _isLoading = MutableStateFlow(false)

    // Observe games and characters from repositories
    val uiState: StateFlow<GamesUiState> = combine(
        gameRepository.games,
        characterRepository.characters,
        _isLoading
    ) { games, characters, isLoading ->
        // Filter characters from game album
        println("truonghehe 1 ${characterRepository.characters.value.size}")
        val gameAlbumCharacters = characters.filterByAlbumId(AppConfig.GAME.GAME_ALBUM_ID)
        println("truonghehe 2 $gameAlbumCharacters")
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
}

