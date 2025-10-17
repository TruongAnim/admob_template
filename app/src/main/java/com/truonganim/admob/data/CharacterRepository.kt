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

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

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
        val jsonString = remoteConfigHelper.getString(RemoteConfigKeys.ALBUM_DATA)
        val characterList = Character.fromJsonArray(jsonString)
        _characters.value = characterList

        // Load favourites and update character list
        loadFavourites()
    }
    
    /**
     * Get characters by album category
     */
    fun getCharactersByAlbum(albumCategory: AlbumCategory): List<Character> {
        return _characters.value.filterByAlbum(albumCategory)
    }
    
    /**
     * Get character by ID
     */
    fun getCharacterById(id: Int): Character? {
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
    fun getFavoriteCharacters(): List<Character> {
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
     * Unlock all characters in an album
     */
    fun unlockAllInAlbum(albumCategory: AlbumCategory) {
        val albumName = when (albumCategory) {
            AlbumCategory.NORMAL -> "normal"
            AlbumCategory.ROLE_PLAY -> "role_play"
            AlbumCategory.HARD -> "hard"
            AlbumCategory.FULL -> "full"
        }
        
        _characters.value = _characters.value.map { character ->
            if (character.album == albumName) {
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

