package com.truonganim.admob.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.IOException

/**
 * Preferences Manager
 * Singleton class to manage DataStore Preferences
 * Easy to use from anywhere in the app
 */
class PreferencesManager private constructor(private val context: Context) {

    // StateFlows for real-time updates
    private val _unlockedCharacterIds = MutableStateFlow<Set<Int>>(emptySet())
    val unlockedCharacterIds: StateFlow<Set<Int>> = _unlockedCharacterIds.asStateFlow()

    private val _characterAdProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val characterAdProgress: StateFlow<Map<Int, Int>> = _characterAdProgress.asStateFlow()

    companion object {
        private const val DATASTORE_NAME = "admob_base_preferences"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }

        // Extension property for DataStore
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
    }
    
    // ==================== Generic Methods ====================
    
    /**
     * Save a value to DataStore
     */
    suspend fun <T> saveValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
        println("💾 Saved to DataStore: ${key.name} = $value")
    }
    
    /**
     * Get a value from DataStore as Flow
     */
    fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }
    
    /**
     * Get a value from DataStore synchronously (suspend function)
     */
    suspend fun <T> getValueSync(key: Preferences.Key<T>, defaultValue: T): T {
        return getValue(key, defaultValue).first()
    }
    
    /**
     * Remove a value from DataStore
     */
    suspend fun <T> removeValue(key: Preferences.Key<T>) {
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
        println("🗑️ Removed from DataStore: ${key.name}")
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        println("🗑️ Cleared all DataStore preferences")
    }
    
    // ==================== Specific Helper Methods ====================
    
    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Flow<Boolean> {
        return getValue(PreferencesKeys.ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Check if onboarding is completed (sync)
     */
    suspend fun isOnboardingCompletedSync(): Boolean {
        return getValueSync(PreferencesKeys.ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        saveValue(PreferencesKeys.ONBOARDING_COMPLETED, completed)
    }
    
    /**
     * Get selected language code
     */
    fun getSelectedLanguageCode(): Flow<String?> {
        return getValue(PreferencesKeys.SELECTED_LANGUAGE_CODE, "")
            .map { if (it.isEmpty()) null else it }
    }
    
    /**
     * Save selected language code
     */
    suspend fun setSelectedLanguageCode(languageCode: String) {
        saveValue(PreferencesKeys.SELECTED_LANGUAGE_CODE, languageCode)
    }
    
    /**
     * Check if this is first launch
     */
    fun isFirstLaunch(): Flow<Boolean> {
        return getValue(PreferencesKeys.FIRST_LAUNCH, true)
    }
    
    /**
     * Mark first launch as completed
     */
    suspend fun setFirstLaunchCompleted() {
        saveValue(PreferencesKeys.FIRST_LAUNCH, false)
    }
    
    /**
     * Get last ad shown time
     */
    suspend fun getLastAdShownTime(): Long {
        return getValueSync(PreferencesKeys.LAST_AD_SHOWN_TIME, 0L)
    }
    
    /**
     * Save last ad shown time
     */
    suspend fun setLastAdShownTime(time: Long) {
        saveValue(PreferencesKeys.LAST_AD_SHOWN_TIME, time)
    }
    
    /**
     * Get ad click count
     */
    suspend fun getAdClickCount(): Int {
        return getValueSync(PreferencesKeys.AD_CLICK_COUNT, 0)
    }
    
    /**
     * Increment ad click count
     */
    suspend fun incrementAdClickCount() {
        val currentCount = getAdClickCount()
        saveValue(PreferencesKeys.AD_CLICK_COUNT, currentCount + 1)
    }

    // ==================== Favourite Methods ====================

    /**
     * Get favourite character IDs
     */
    suspend fun getFavouriteCharacterIds(): Set<Int> {
        val idsString = getValueSync(PreferencesKeys.FAVOURITE_CHARACTER_IDS, "")
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    /**
     * Save favourite character IDs
     */
    suspend fun saveFavouriteCharacterIds(ids: Set<Int>) {
        val idsString = ids.joinToString(",")
        saveValue(PreferencesKeys.FAVOURITE_CHARACTER_IDS, idsString)
    }

    /**
     * Get favourite photo URLs
     */
    suspend fun getFavouritePhotoUrls(): Set<String> {
        val urlsString = getValueSync(PreferencesKeys.FAVOURITE_PHOTO_URLS, "")
        return if (urlsString.isEmpty()) {
            emptySet()
        } else {
            urlsString.split("|||").filter { it.isNotEmpty() }.toSet()
        }
    }

    /**
     * Save favourite photo URLs
     */
    suspend fun saveFavouritePhotoUrls(urls: Set<String>) {
        val urlsString = urls.joinToString("|||")
        saveValue(PreferencesKeys.FAVOURITE_PHOTO_URLS, urlsString)
    }

    // ==================== Album Methods ====================

    /**
     * Get album watched ad counts (JSON string)
     */
    fun getAlbumWatchedAdCounts(): Flow<String> {
        return getValue(PreferencesKeys.ALBUM_WATCHED_AD_COUNTS, "{}")
    }

    /**
     * Save album watched ad counts (JSON string)
     */
    suspend fun saveAlbumWatchedAdCounts(json: String) {
        saveValue(PreferencesKeys.ALBUM_WATCHED_AD_COUNTS, json)
    }

    // ==================== Notification Permission Methods ====================

    /**
     * Check if notification permission has been requested
     */
    suspend fun isNotificationPermissionRequested(): Boolean {
        return getValueSync(PreferencesKeys.NOTIFICATION_PERMISSION_REQUESTED, false)
    }

    /**
     * Mark notification permission as requested
     */
    suspend fun setNotificationPermissionRequested(requested: Boolean) {
        saveValue(PreferencesKeys.NOTIFICATION_PERMISSION_REQUESTED, requested)
    }

    /**
     * Check if notification permission dialog has been shown
     */
    suspend fun isNotificationPermissionDialogShown(): Boolean {
        return getValueSync(PreferencesKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN, false)
    }

    /**
     * Mark notification permission dialog as shown
     */
    suspend fun setNotificationPermissionDialogShown(shown: Boolean) {
        saveValue(PreferencesKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN, shown)
    }

    // ==================== Character Unlock Methods ====================

    /**
     * Load unlocked character IDs from DataStore
     */
    suspend fun loadUnlockedCharacterIds() {
        val idsString = getValueSync(PreferencesKeys.UNLOCKED_CHARACTER_IDS, "")
        _unlockedCharacterIds.value = if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    /**
     * Get unlocked character IDs (sync)
     */
    suspend fun getUnlockedCharacterIds(): Set<Int> {
        val idsString = getValueSync(PreferencesKeys.UNLOCKED_CHARACTER_IDS, "")
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    /**
     * Unlock a character permanently
     */
    suspend fun unlockCharacter(characterId: Int) {
        val currentIds = _unlockedCharacterIds.value.toMutableSet()
        currentIds.add(characterId)
        _unlockedCharacterIds.value = currentIds

        val idsString = currentIds.joinToString(",")
        saveValue(PreferencesKeys.UNLOCKED_CHARACTER_IDS, idsString)
        println("🔓 Character $characterId unlocked permanently $currentIds")
    }

    /**
     * Check if a character is unlocked
     */
    fun isCharacterUnlocked(characterId: Int): Boolean {
        return _unlockedCharacterIds.value.contains(characterId)
    }

    /**
     * Load character ad progress from DataStore
     */
    suspend fun loadCharacterAdProgress() {
        val jsonString = getValueSync(PreferencesKeys.CHARACTER_AD_PROGRESS, "{}")
        _characterAdProgress.value = try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<Int, Int>()
            json.keys().forEach { key ->
                val characterId = key.toIntOrNull()
                if (characterId != null) {
                    map[characterId] = json.getInt(key)
                }
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Get character ad progress (sync)
     */
    suspend fun getCharacterAdProgress(): Map<Int, Int> {
        val jsonString = getValueSync(PreferencesKeys.CHARACTER_AD_PROGRESS, "{}")
        return try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<Int, Int>()
            json.keys().forEach { key ->
                val characterId = key.toIntOrNull()
                if (characterId != null) {
                    map[characterId] = json.getInt(key)
                }
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Increment ad progress for a character
     * Returns true if character should be unlocked (reached required count)
     */
    suspend fun incrementCharacterAdProgress(characterId: Int, requiredAdCount: Int): Boolean {
        val currentProgress = _characterAdProgress.value.toMutableMap()
        val currentCount = currentProgress[characterId] ?: 0
        val newCount = currentCount + 1

        currentProgress[characterId] = newCount
        _characterAdProgress.value = currentProgress

        // Save to DataStore
        val json = JSONObject()
        currentProgress.forEach { (id, count) ->
            json.put(id.toString(), count)
        }
        saveValue(PreferencesKeys.CHARACTER_AD_PROGRESS, json.toString())

        println("📺 Character $characterId ad progress: $newCount/$requiredAdCount")

        // Check if should unlock
        if (newCount >= requiredAdCount) {
            unlockCharacter(characterId)
            // Clear progress for this character
            currentProgress.remove(characterId)
            _characterAdProgress.value = currentProgress
            val updatedJson = JSONObject()
            currentProgress.forEach { (id, count) ->
                updatedJson.put(id.toString(), count)
            }
            saveValue(PreferencesKeys.CHARACTER_AD_PROGRESS, updatedJson.toString())
            return true
        }

        return false
    }

    /**
     * Get ad progress for a specific character
     */
    fun getCharacterAdCount(characterId: Int): Int {
        return _characterAdProgress.value[characterId] ?: 0
    }
}

