package com.truonganim.admob.ui.albumdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.filterByAlbum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Album Detail Screen UI State
 */
data class AlbumDetailUiState(
    val albumCategory: AlbumCategory = AlbumCategory.NORMAL,
    val appCharacters: List<AppCharacter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Album Detail ViewModel
 */
class AlbumDetailViewModel(
    private val albumCategory: AlbumCategory,
    private val context: Context
) : ViewModel() {

    private val characterRepository = CharacterRepository.getInstance(context)

    private val _isLoading = MutableStateFlow(false)

    // Observe characters from repository and filter by album category
    val uiState: StateFlow<AlbumDetailUiState> = combine(
        characterRepository.characters,
        _isLoading
    ) { characters, isLoading ->
        val filteredCharacters = if (albumCategory == AlbumCategory.FAVOURITE) {
            characters.filter { it.isFavorite }
        } else {
            characters.filterByAlbum(albumCategory)
        }

        AlbumDetailUiState(
            albumCategory = albumCategory,
            appCharacters = filteredCharacters,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumDetailUiState(albumCategory = albumCategory)
    )

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load characters from repository
                characterRepository.loadCharacters()
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
    
    fun onFavoriteClick(appCharacter: AppCharacter) {
        viewModelScope.launch {
            characterRepository.toggleFavorite(appCharacter.id)
            // UI will auto-update via StateFlow
        }
    }

    fun onUnlockAllClick() {
        characterRepository.unlockAllInAlbum(albumCategory)
        // UI will auto-update via StateFlow
    }
}

