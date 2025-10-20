package com.truonganim.admob.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Album
import com.truonganim.admob.data.AlbumRepository
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.utils.NotificationPermissionHelper
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
    val error: String? = null,
    val showNotificationDialog: Boolean = false,
    val showNotificationBanner: Boolean = false
)

/**
 * Albums ViewModel
 */
class AlbumsViewModel(private val context: Context) : ViewModel() {

    private val albumRepository = AlbumRepository.getInstance(context)
    private val preferencesManager = PreferencesManager.getInstance(context)

    private val _isLoading = MutableStateFlow(false)
    private val _showNotificationDialog = MutableStateFlow(false)
    private val _showNotificationBanner = MutableStateFlow(false)

    // Observe albums from repository
    val uiState: StateFlow<AlbumsUiState> = combine(
        albumRepository.albums,
        _isLoading,
        _showNotificationDialog,
        _showNotificationBanner
    ) { albums, isLoading, showDialog, showBanner ->
        AlbumsUiState(
            albums = albums,
            isLoading = isLoading,
            showNotificationDialog = showDialog,
            showNotificationBanner = showBanner
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

    /**
     * Check notification permission and update UI state
     */
    fun checkNotificationPermission() {
        viewModelScope.launch {
            val isGranted = NotificationPermissionHelper.isNotificationPermissionGranted(context)
            val dialogShown = preferencesManager.isNotificationPermissionDialogShown()

            if (!isGranted) {
                if (!dialogShown) {
                    // Show dialog if not shown before
                    _showNotificationDialog.value = true
                    _showNotificationBanner.value = false
                } else {
                    // Show banner if dialog was already shown
                    _showNotificationDialog.value = false
                    _showNotificationBanner.value = true
                }
            } else {
                // Permission granted, hide both
                _showNotificationDialog.value = false
                _showNotificationBanner.value = false
            }
        }
    }

    /**
     * Handle permission result
     */
    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (!granted) {
                // Permission denied, show dialog
                val dialogShown = preferencesManager.isNotificationPermissionDialogShown()

                if (!dialogShown) {
                    _showNotificationDialog.value = true
                    _showNotificationBanner.value = false
                } else {
                    _showNotificationDialog.value = false
                    _showNotificationBanner.value = true
                }
            } else {
                // Permission granted
                _showNotificationDialog.value = false
                _showNotificationBanner.value = false
            }
        }
    }

    /**
     * Dismiss notification dialog
     */
    fun dismissNotificationDialog() {
        viewModelScope.launch {
            _showNotificationDialog.value = false
            _showNotificationBanner.value = true
            preferencesManager.setNotificationPermissionDialogShown(true)
        }
    }

    fun onAlbumClick(album: Album) {
        // Navigation is handled by the screen composable
    }
}

