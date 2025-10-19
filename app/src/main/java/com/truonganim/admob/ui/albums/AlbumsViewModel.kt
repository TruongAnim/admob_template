package com.truonganim.admob.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Album
import com.truonganim.admob.data.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
class AlbumsViewModel(private val context: Context) : ViewModel() {

    private val albumRepository = AlbumRepository.getInstance(context)

    private val _isLoading = MutableStateFlow(false)

    // Observe albums from repository
    val uiState: StateFlow<AlbumsUiState> = combine(
        albumRepository.albums,
        _isLoading
    ) { albums, isLoading ->
        AlbumsUiState(
            albums = albums,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState()
    )

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                albumRepository.loadAlbums()
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAlbumClick(album: Album) {
        // Navigation is handled by the screen composable
    }
}

