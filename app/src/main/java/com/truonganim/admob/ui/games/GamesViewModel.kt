package com.truonganim.admob.ui.games

import androidx.lifecycle.ViewModel
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.SampleGames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
class GamesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        _uiState.value = _uiState.value.copy(
            games = SampleGames.getGames(),
            isLoading = false
        )
    }
}

