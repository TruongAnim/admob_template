package com.truonganim.admob.ui.albumdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.truonganim.admob.data.AlbumCategory

/**
 * Factory for creating AlbumDetailViewModel with parameters
 */
class AlbumDetailViewModelFactory(
    private val albumCategory: AlbumCategory,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumDetailViewModel::class.java)) {
            return AlbumDetailViewModel(albumCategory, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

