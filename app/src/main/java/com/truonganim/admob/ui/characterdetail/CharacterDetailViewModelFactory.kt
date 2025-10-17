package com.truonganim.admob.ui.characterdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating CharacterDetailViewModel with parameters
 */
class CharacterDetailViewModelFactory(
    private val characterId: Int,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
            return CharacterDetailViewModel(characterId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

