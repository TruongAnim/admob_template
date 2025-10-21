package com.truonganim.admob.ui.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameRepository
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
    val isLoading: Boolean = false
)

/**
 * Games ViewModel
 */
class GamesViewModel(context: Context) : ViewModel() {

    private val gameRepository = GameRepository.getInstance(context)
    private val _isLoading = MutableStateFlow(false)

    // Observe games from repository
    val uiState: StateFlow<GamesUiState> = combine(
        gameRepository.games,
        _isLoading
    ) { games, isLoading ->
        GamesUiState(
            games = games,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamesUiState()
    )

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                gameRepository.loadGames()
            } catch (e: Exception) {
                // Handle error if needed
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

