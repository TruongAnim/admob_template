package com.truonganim.admob.ui.photoviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating PhotoViewerViewModel with parameters
 */
class PhotoViewerViewModelFactory(
    private val characterId: Int,
    private val initialPhotoIndex: Int
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoViewerViewModel::class.java)) {
            return PhotoViewerViewModel(characterId, initialPhotoIndex) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

