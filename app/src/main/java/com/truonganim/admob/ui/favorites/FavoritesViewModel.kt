package com.truonganim.admob.ui.favorites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Favorites Screen UI State
 */
data class FavoritesUiState(
    val favoriteCount: Int = 0,
    val isLoading: Boolean = false
)

/**
 * Favorites ViewModel
 */
class FavoritesViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // TODO: Load favorites from repository
        
        _uiState.value = _uiState.value.copy(
            favoriteCount = 0,
            isLoading = false
        )
    }
}

