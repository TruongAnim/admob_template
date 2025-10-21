package com.truonganim.admob.ui.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
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

    private val _isLoading = MutableStateFlow(false)

    // Observe both characters and photos from repositories
    val uiState: StateFlow<FavoritesUiState> = combine(
        characterRepository.characters,
        photoRepository.favouritePhotoUrls,
        _isLoading
    ) { characters, favouritePhotoUrls, isLoading ->
        println("truonghehe 3 ${characterRepository.characters.value.size}")
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

    fun onCharacterClick(appCharacter: AppCharacter) {
        // Navigation is handled by the screen composable
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

