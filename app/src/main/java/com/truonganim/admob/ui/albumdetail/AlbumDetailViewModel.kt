package com.truonganim.admob.ui.albumdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.data.Character
import com.truonganim.admob.data.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Album Detail Screen UI State
 */
data class AlbumDetailUiState(
    val albumCategory: AlbumCategory = AlbumCategory.NORMAL,
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Album Detail ViewModel
 */
class AlbumDetailViewModel(
    private val albumCategory: AlbumCategory
) : ViewModel() {
    
    private val characterRepository = CharacterRepository.getInstance()
    
    private val _uiState = MutableStateFlow(AlbumDetailUiState(albumCategory = albumCategory))
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadCharacters()
    }
    
    private fun loadCharacters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load characters from repository
                characterRepository.loadCharacters()
                
                // Filter by album category
                val characters = characterRepository.getCharactersByAlbum(albumCategory)
                
                _uiState.value = _uiState.value.copy(
                    characters = characters,
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
    
    fun onCharacterClick(character: Character) {
        // TODO: Navigate to character detail screen
        // This will be implemented later
    }
    
    fun onFavoriteClick(character: Character) {
        characterRepository.toggleFavorite(character.id)
        // Reload characters to update UI
        loadCharacters()
    }
    
    fun onUnlockAllClick() {
        characterRepository.unlockAllInAlbum(albumCategory)
        // Reload characters to update UI
        loadCharacters()
    }
}

