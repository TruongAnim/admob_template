package com.truonganim.admob.ui.albumdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.AlbumRepository
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.filterByAlbumId
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
    val albumId: String = "",
    val albumName: String = "",
    val appCharacters: List<AppCharacter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Album Detail ViewModel
 */
class AlbumDetailViewModel(
    private val albumId: String,
    private val context: Context
) : ViewModel() {

    private val characterRepository = CharacterRepository.getInstance(context)
    private val albumRepository = AlbumRepository.getInstance(context)

    private val _isLoading = MutableStateFlow(false)

    // Observe characters from repository and filter by album ID
    val uiState: StateFlow<AlbumDetailUiState> = combine(
        characterRepository.characters,
        albumRepository.albums,
        _isLoading
    ) { characters, albums, isLoading ->
        val album = albums.find { it.albumId == albumId }
        val filteredCharacters = if (albumId == "favourite") {
            characters.filter { it.isFavorite }
        } else {
            characters.filterByAlbumId(albumId)
        }

        AlbumDetailUiState(
            albumId = albumId,
            albumName = album?.name ?: albumId.uppercase(),
            appCharacters = filteredCharacters,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumDetailUiState(albumId = albumId)
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
        characterRepository.unlockAllInAlbum(albumId)
        // UI will auto-update via StateFlow
    }
}

