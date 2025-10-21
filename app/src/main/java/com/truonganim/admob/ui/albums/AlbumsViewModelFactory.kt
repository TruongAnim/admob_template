package com.truonganim.admob.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating AlbumsViewModel with parameters
 */
class AlbumsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumsViewModel::class.java)) {
            return AlbumsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

