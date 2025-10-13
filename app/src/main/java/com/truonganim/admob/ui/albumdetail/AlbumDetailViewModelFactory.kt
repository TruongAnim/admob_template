package com.truonganim.admob.ui.albumdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.truonganim.admob.data.AlbumCategory

/**
 * Factory for creating AlbumDetailViewModel with parameters
 */
class AlbumDetailViewModelFactory(
    private val albumCategory: AlbumCategory
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumDetailViewModel::class.java)) {
            return AlbumDetailViewModel(albumCategory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

