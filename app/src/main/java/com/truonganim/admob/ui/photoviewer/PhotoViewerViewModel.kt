package com.truonganim.admob.ui.photoviewer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.config.AppConfig
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.PhotoRepository
import com.truonganim.admob.utils.ImageUtils
import com.truonganim.admob.utils.WallpaperHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Photo Viewer
 */
data class PhotoViewerUiState(
    val photos: List<String> = emptyList(),
    val currentPhotoIndex: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val isSettingWallpaper: Boolean = false
)

/**
 * ViewModel for Photo Viewer Screen
 */
class PhotoViewerViewModel(
    private val characterId: Int,
    private val initialPhotoIndex: Int,
    private val context: Context
) : ViewModel() {

    private val characterRepository = CharacterRepository.getInstance(context)
    private val photoRepository = PhotoRepository.getInstance(context)

    private val _uiState = MutableStateFlow(PhotoViewerUiState())
    val uiState: StateFlow<PhotoViewerUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val photos = if (characterId == AppCharacter.FAVOURITE_PHOTOS_ID) {
                // Load favourite photos for special FAVOURITE_PHOTOS character
                photoRepository.loadFavourites()
                photoRepository.getFavouritePhotos()
            } else {
                // Load photos from character
                val character = characterRepository.getCharacterById(characterId)
                character?.photos ?: emptyList()
            }

            _uiState.value = _uiState.value.copy(
                photos = photos,
                currentPhotoIndex = initialPhotoIndex.coerceIn(0, photos.size - 1),
                isLoading = false
            )
        }
    }
    
    /**
     * Update current photo index when user swipes
     */
    fun onPhotoIndexChanged(index: Int) {
        _uiState.value = _uiState.value.copy(currentPhotoIndex = index)
    }
    
    /**
     * Save current photo to gallery
     */
    fun onSaveClick(context: Context, onPermissionNeeded: () -> Unit) {
        // Check permission
        if (needsStoragePermission(context)) {
            onPermissionNeeded()
            return
        }
        
        val currentPhoto = _uiState.value.photos.getOrNull(_uiState.value.currentPhotoIndex)
        if (currentPhoto == null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            ImageUtils.saveImageToGallery(
                context = context,
                imageUrl = currentPhoto
            )
            
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
    
    /**
     * Share current photo
     */
    fun onShareClick(context: Context) {
        val currentPhoto = _uiState.value.photos.getOrNull(_uiState.value.currentPhotoIndex)
        if (currentPhoto == null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true)
            
            ImageUtils.shareImage(
                context = context,
                imageUrl = currentPhoto
            )
            
            _uiState.value = _uiState.value.copy(isSharing = false)
        }
    }
    
    /**
     * Set current photo as wallpaper
     * Uses the method configured in AppConfig.Wallpaper.SET_WALLPAPER_METHOD
     */
    fun onSetWallpaperClick(context: Context) {
        val currentPhoto = _uiState.value.photos.getOrNull(_uiState.value.currentPhotoIndex)
        if (currentPhoto == null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSettingWallpaper = true)

            WallpaperHelper.setWallpaperFromUrl(
                context = context,
                imageUrl = currentPhoto,
                wallpaperType = AppConfig.Wallpaper.DEFAULT_WALLPAPER_TYPE,
                method = AppConfig.Wallpaper.SET_WALLPAPER_METHOD
            )

            _uiState.value = _uiState.value.copy(isSettingWallpaper = false)
        }
    }

    /**
     * Check if storage permission is needed
     */
    private fun needsStoragePermission(context: Context): Boolean {
        // Android 13+ doesn't need WRITE_EXTERNAL_STORAGE for MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return false
        }

        // Android 10-12 doesn't need permission when using MediaStore with scoped storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false
        }

        // Android 9 and below needs WRITE_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }
}

