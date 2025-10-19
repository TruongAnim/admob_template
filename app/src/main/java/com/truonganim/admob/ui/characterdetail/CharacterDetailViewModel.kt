package com.truonganim.admob.ui.characterdetail

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
 * Character Detail Screen UI State
 */
data class CharacterDetailUiState(
    val appCharacter: AppCharacter? = null,
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

    private val _isLoading = MutableStateFlow(false)

    // Observe both characters and favourite photos from repositories
    val uiState: StateFlow<CharacterDetailUiState> = combine(
        characterRepository.characters,
        photoRepository.favouritePhotoUrls,
        _isLoading
    ) { characters, favouritePhotoUrls, isLoading ->
        val character = if (characterId == AppCharacter.FAVOURITE_PHOTOS_ID) {
            // Create special character for favourite photos
            AppCharacter(
                id = AppCharacter.FAVOURITE_PHOTOS_ID,
                name = "Favourite Photos",
                album = "favourite",
                order = 0,
                adCount = 0,
                thumbnail = favouritePhotoUrls.firstOrNull() ?: "",
                photos = favouritePhotoUrls.toList(),
                isFavorite = false,
                isUnlocked = true,
                currentPhotoIndex = 0
            )
        } else {
            characters.find { it.id == characterId }
        }

        CharacterDetailUiState(
            appCharacter = character,
            favouritePhotoUrls = favouritePhotoUrls,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CharacterDetailUiState()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load characters and favourite photos
                characterRepository.loadCharacters()
                photoRepository.loadFavourites()
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onPhotoClick(photoIndex: Int) {
        // Navigation is handled by the screen composable
    }

    fun onCharacterFavoriteClick() {
        viewModelScope.launch {
            uiState.value.appCharacter?.let { character ->
                characterRepository.toggleFavorite(character.id)
                // UI will auto-update via StateFlow
            }
        }
    }

    fun onPhotoFavoriteClick(photoUrl: String) {
        viewModelScope.launch {
            photoRepository.toggleFavourite(photoUrl)
            // UI will auto-update via StateFlow
        }
    }
}

