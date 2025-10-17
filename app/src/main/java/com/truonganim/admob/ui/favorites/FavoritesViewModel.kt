package com.truonganim.admob.ui.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Character
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Favorites Screen UI State
 */
data class FavoritesUiState(
    val favoriteCharacters: List<Character> = emptyList(),
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

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load characters and favourites
            characterRepository.loadCharacters()
            photoRepository.loadFavourites()

            // Get favourite characters
            val favouriteCharacters = characterRepository.getFavoriteCharacters()

            // Get favourite photos
            val favouritePhotos = photoRepository.getFavouritePhotos()

            _uiState.value = _uiState.value.copy(
                favoriteCharacters = favouriteCharacters,
                favoritePhotos = favouritePhotos,
                isLoading = false
            )
        }
    }

    fun onCharacterClick(character: Character) {
        // Navigation is handled by the screen composable
    }

    fun onPhotoClick(photoUrl: String) {
        // Navigation is handled by the screen composable
    }

    fun onCharacterFavoriteClick(character: Character) {
        viewModelScope.launch {
            characterRepository.toggleFavorite(character.id)
            loadFavorites()
        }
    }

    fun onPhotoFavoriteClick(photoUrl: String) {
        viewModelScope.launch {
            photoRepository.toggleFavourite(photoUrl)
            loadFavorites()
        }
    }
}

