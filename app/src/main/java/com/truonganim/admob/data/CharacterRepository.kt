package com.truonganim.admob.data

import android.content.Context
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Repository for managing Character data
 */
class CharacterRepository private constructor(private val context: Context) {

    private val remoteConfigHelper = RemoteConfigHelper.getInstance()
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _baseCharacters = MutableStateFlow<List<AppCharacter>>(emptyList())

    // Combine base characters with unlock status and ad progress
    val characters: StateFlow<List<AppCharacter>> = combine(
        _baseCharacters,
        preferencesManager.unlockedCharacterIds,
        preferencesManager.characterAdProgress
    ) { baseChars, unlockedIds, adProgress ->
        baseChars.map { character ->
            val isUnlocked = unlockedIds.contains(character.id) || character.isUnlocked
            character.copy(isUnlocked = isUnlocked)
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()
    
    /**
     * Load favourite character IDs from DataStore
     */
    suspend fun loadFavourites() {
        val favourites = preferencesManager.getFavouriteCharacterIds()
        _favorites.value = favourites

        // Update character list with favourite status
        _baseCharacters.value = _baseCharacters.value.map { character ->
            character.copy(isFavorite = favourites.contains(character.id))
        }
    }

    /**
     * Load characters from Remote Config
     */
    suspend fun loadCharacters() {
        val jsonString = remoteConfigHelper.getString(RemoteConfigKeys.CHARACTERS_DATA)
        val appCharacterList = AppCharacter.fromJsonArray(jsonString)
        _baseCharacters.value = appCharacterList

        // Load unlock status and ad progress
        preferencesManager.loadUnlockedCharacterIds()
        preferencesManager.loadCharacterAdProgress()

        // Load favourites and update character list
        loadFavourites()
    }
    
    /**
     * Get character by ID
     */
    fun getCharacterById(id: Int): AppCharacter? {
        return characters.value.find { it.id == id }
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
        _baseCharacters.value = _baseCharacters.value.map { character ->
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
        return characters.value.filter { it.isFavorite }
    }

    /**
     * Unlock character permanently (by game win)
     */
    suspend fun unlockCharacterByGame(characterId: Int) {
        preferencesManager.unlockCharacter(characterId)
        println("🎮 Character $characterId unlocked by game")
    }

    /**
     * Increment ad progress and unlock if reached required count
     * Returns true if character was unlocked
     */
    suspend fun unlockCharacterByAd(characterId: Int): Boolean {
        val character = getCharacterById(characterId) ?: return false
        val requiredAdCount = character.adCount

        if (requiredAdCount <= 0) return false

        val unlocked = preferencesManager.incrementCharacterAdProgress(characterId, requiredAdCount)
        return unlocked
    }

    /**
     * Get current ad progress for a character
     */
    fun getCharacterAdProgress(characterId: Int): Int {
        return preferencesManager.getCharacterAdCount(characterId)
    }

    /**
     * Unlock all characters in an album by albumId
     */
    fun unlockAllInAlbum(albumId: String) {
        repositoryScope.launch {
            val charactersInAlbum = characters.value.filter { it.album == albumId }
            charactersInAlbum.forEach { character ->
                preferencesManager.unlockCharacter(character.id)
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

