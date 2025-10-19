package com.truonganim.admob.ui.characterdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.CharacterRepository
import com.truonganim.admob.data.PhotoRepository
import com.truonganim.admob.wallpaper.AppWallpaperManager
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
    val isSettingWallpaper: Boolean = false,
    val wallpaperProgress: Pair<Int, Int>? = null, // current, total
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
    private val wallpaperManager = AppWallpaperManager.getInstance(context)

    private val _isLoading = MutableStateFlow(false)
    private val _isSettingWallpaper = MutableStateFlow(false)
    private val _wallpaperProgress = MutableStateFlow<Pair<Int, Int>?>(null)

    // Observe both characters and favourite photos from repositories
    val uiState: StateFlow<CharacterDetailUiState> = combine(
        characterRepository.characters,
        photoRepository.favouritePhotoUrls,
        _isLoading,
        _isSettingWallpaper,
        _wallpaperProgress
    ) { characters, favouritePhotoUrls, isLoading, isSettingWallpaper, wallpaperProgress ->
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
            isLoading = isLoading,
            isSettingWallpaper = isSettingWallpaper,
            wallpaperProgress = wallpaperProgress
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

    /**
     * Set all photos in current character as random live wallpaper
     */
    fun onSetRandomWallpaperClick() {
        val photos = uiState.value.appCharacter?.photos
        if (photos.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            _isSettingWallpaper.value = true
            _wallpaperProgress.value = null

            try {
                val success = wallpaperManager.setupRandomWallpaper(
                    imageUrls = photos,
                    intervalSeconds = 15, // Change wallpaper every 15 seconds
                    onProgress = { current, total ->
                        _wallpaperProgress.value = Pair(current, total)
                    }
                )

                if (success) {
                    // Launch wallpaper picker to set the live wallpaper
                    wallpaperManager.launchWallpaperPicker()
                }
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isSettingWallpaper.value = false
                _wallpaperProgress.value = null
            }
        }
    }
}

