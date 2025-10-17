package com.truonganim.admob.ui.characterdetail

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
 * Character Detail Screen UI State
 */
data class CharacterDetailUiState(
    val character: Character? = null,
    val favouritePhotoUrls: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Character Detail ViewModel
 */
class CharacterDetailViewModel(
    private val characterId: Int,
    private val context: Context
) : ViewModel() {

    private val characterRepository = CharacterRepository.getInstance(context)
    private val photoRepository = PhotoRepository.getInstance(context)

    private val _uiState = MutableStateFlow(CharacterDetailUiState())
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    init {
        loadCharacter()
        loadFavouritePhotos()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val character = characterRepository.getCharacterById(characterId)
                _uiState.value = _uiState.value.copy(
                    character = character,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun loadFavouritePhotos() {
        viewModelScope.launch {
            photoRepository.loadFavourites()
            photoRepository.favouritePhotoUrls.collect { favourites ->
                _uiState.value = _uiState.value.copy(favouritePhotoUrls = favourites)
            }
        }
    }

    fun onPhotoClick(photoIndex: Int) {
        // Navigation is handled by the screen composable
    }

    fun onCharacterFavoriteClick() {
        viewModelScope.launch {
            _uiState.value.character?.let { character ->
                characterRepository.toggleFavorite(character.id)
                // Reload character to update UI
                loadCharacter()
            }
        }
    }

    fun onPhotoFavoriteClick(photoUrl: String) {
        viewModelScope.launch {
            photoRepository.toggleFavourite(photoUrl)
        }
    }
}

