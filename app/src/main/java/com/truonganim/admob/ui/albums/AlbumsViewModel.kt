package com.truonganim.admob.ui.albums

import androidx.lifecycle.ViewModel
import com.truonganim.admob.data.Album
import com.truonganim.admob.data.SampleAlbums
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Albums Screen UI State
 */
data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Albums ViewModel
 */
class AlbumsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()
    
    init {
        loadAlbums()
    }
    
    private fun loadAlbums() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Simulate loading albums
        val albums = SampleAlbums.getAlbums()
        
        _uiState.value = _uiState.value.copy(
            albums = albums,
            isLoading = false
        )
    }
    
    fun onAlbumClick(album: Album) {
        // TODO: Handle album click - will be implemented later
    }
}

