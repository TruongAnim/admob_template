package com.truonganim.admob.data

import android.content.Context
import com.truonganim.admob.datastore.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing Photo favourites
 */
class PhotoRepository private constructor(private val context: Context) {
    
    private val preferencesManager = PreferencesManager.getInstance(context)
    
    private val _favouritePhotoUrls = MutableStateFlow<Set<String>>(emptySet())
    val favouritePhotoUrls: StateFlow<Set<String>> = _favouritePhotoUrls.asStateFlow()
    
    /**
     * Load favourite photo URLs from DataStore
     */
    suspend fun loadFavourites() {
        val favourites = preferencesManager.getFavouritePhotoUrls()
        _favouritePhotoUrls.value = favourites
    }
    
    /**
     * Toggle favourite status for a photo
     */
    suspend fun toggleFavourite(photoUrl: String) {
        val currentFavourites = _favouritePhotoUrls.value.toMutableSet()
        if (currentFavourites.contains(photoUrl)) {
            currentFavourites.remove(photoUrl)
        } else {
            currentFavourites.add(photoUrl)
        }
        _favouritePhotoUrls.value = currentFavourites
        
        // Save to DataStore
        preferencesManager.saveFavouritePhotoUrls(currentFavourites)
    }
    
    /**
     * Check if photo is favourite
     */
    fun isFavourite(photoUrl: String): Boolean {
        return _favouritePhotoUrls.value.contains(photoUrl)
    }
    
    /**
     * Get all favourite photo URLs
     */
    fun getFavouritePhotos(): List<String> {
        return _favouritePhotoUrls.value.toList()
    }
    
    companion object {
        @Volatile
        private var instance: PhotoRepository? = null
        
        fun getInstance(context: Context): PhotoRepository {
            return instance ?: synchronized(this) {
                instance ?: PhotoRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

