package com.truonganim.admob.data

import android.content.Context
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing Character data
 */
class CharacterRepository private constructor(private val context: Context) {

    private val remoteConfigHelper = RemoteConfigHelper.getInstance()
    private val preferencesManager = PreferencesManager.getInstance(context)

    private val _characters = MutableStateFlow<List<AppCharacter>>(emptyList())
    val characters: StateFlow<List<AppCharacter>> = _characters.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()
    
    /**
     * Load favourite character IDs from DataStore
     */
    suspend fun loadFavourites() {
        val favourites = preferencesManager.getFavouriteCharacterIds()
        _favorites.value = favourites

        // Update character list with favourite status
        _characters.value = _characters.value.map { character ->
            character.copy(isFavorite = favourites.contains(character.id))
        }
    }

    /**
     * Load characters from Remote Config
     */
    suspend fun loadCharacters() {
        val jsonString = remoteConfigHelper.getString(RemoteConfigKeys.CHARACTERS_DATA)
        val appCharacterList = AppCharacter.fromJsonArray(jsonString)
        _characters.value = appCharacterList

        // Load favourites and update character list
        loadFavourites()
    }
    
    /**
     * Get character by ID
     */
    fun getCharacterById(id: Int): AppCharacter? {
        return _characters.value.find { it.id == id }
    }
    
    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(characterId: Int) {
        val currentFavorites = _favorites.value.toMutableSet()
        if (currentFavorites.contains(characterId)) {
            currentFavorites.remove(characterId)
        } else {
            currentFavorites.add(characterId)
        }
        _favorites.value = currentFavorites

        // Save to DataStore
        preferencesManager.saveFavouriteCharacterIds(currentFavorites)

        // Update character list
        _characters.value = _characters.value.map { character ->
            if (character.id == characterId) {
                character.copy(isFavorite = currentFavorites.contains(characterId))
            } else {
                character
            }
        }
    }
    
    /**
     * Check if character is favorite
     */
    fun isFavorite(characterId: Int): Boolean {
        return _favorites.value.contains(characterId)
    }
    
    /**
     * Get all favorite characters
     */
    fun getFavoriteCharacters(): List<AppCharacter> {
        return _characters.value.filter { it.isFavorite }
    }
    
    /**
     * Unlock character
     */
    fun unlockCharacter(characterId: Int) {
        _characters.value = _characters.value.map { character ->
            if (character.id == characterId) {
                character.copy(isUnlocked = true)
            } else {
                character
            }
        }
    }

    /**
     * Unlock all characters in an album by albumId
     */
    fun unlockAllInAlbum(albumId: String) {
        unlockAllInAlbumById(albumId)
    }

    /**
     * Internal method to unlock all characters in an album
     */
    private fun unlockAllInAlbumById(albumId: String) {
        _characters.value = _characters.value.map { character ->
            if (character.album == albumId) {
                character.copy(isUnlocked = true)
            } else {
                character
            }
        }
    }
    
    companion object {
        @Volatile
        private var instance: CharacterRepository? = null

        fun getInstance(context: Context): CharacterRepository {
            return instance ?: synchronized(this) {
                instance ?: CharacterRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

