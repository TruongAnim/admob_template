package com.truonganim.admob.ui.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Character
import com.truonganim.admob.data.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Character Detail Screen UI State
 */
data class CharacterDetailUiState(
    val character: Character? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Character Detail ViewModel
 */
class CharacterDetailViewModel(
    private val characterId: Int
) : ViewModel() {
    
    private val characterRepository = CharacterRepository.getInstance()
    
    private val _uiState = MutableStateFlow(CharacterDetailUiState())
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadCharacter()
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
    
    fun onPhotoClick(photoIndex: Int) {
        // TODO: Open full screen photo viewer
        // This will be implemented later
    }
    
    fun onFavoriteClick() {
        _uiState.value.character?.let { character ->
            characterRepository.toggleFavorite(character.id)
            // Reload character to update UI
            loadCharacter()
        }
    }
}

