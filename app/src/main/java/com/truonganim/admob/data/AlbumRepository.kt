package com.truonganim.admob.data

import android.content.Context
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/**
 * Album Repository
 * Manages album data from Remote Config and user progress
 */
class AlbumRepository private constructor(private val context: Context) {
    
    private val remoteConfigHelper = RemoteConfigHelper.getInstance()
    private val preferencesManager = PreferencesManager.getInstance(context)
    
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    
    /**
     * Load albums from Remote Config
     */
    suspend fun loadAlbums() {
        val jsonString = remoteConfigHelper.getString(RemoteConfigKeys.ALBUMS_DATA)
        println("truonganim" + jsonString)
        val albumList = Album.fromJsonArray(jsonString)
        
        // Load watched ad counts from preferences
        val watchedAdCounts = loadWatchedAdCounts()
        
        // Update albums with watched ad counts
        _albums.value = albumList.map { album ->
            album.copy(watchedAdCount = watchedAdCounts[album.id] ?: 0)
        }
    }
    
    /**
     * Load watched ad counts from preferences
     */
    private suspend fun loadWatchedAdCounts(): Map<String, Int> {
        val json = preferencesManager.getAlbumWatchedAdCounts().first()
        return try {
            val jsonObject = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.getInt(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Save watched ad counts to preferences
     */
    private suspend fun saveWatchedAdCounts() {
        val map = _albums.value.associate { it.id to it.watchedAdCount }
        val jsonObject = JSONObject(map)
        preferencesManager.saveAlbumWatchedAdCounts(jsonObject.toString())
    }
    
    /**
     * Increment watched ad count for an album
     */
    suspend fun incrementWatchedAdCount(albumId: String) {
        _albums.value = _albums.value.map { album ->
            if (album.id == albumId) {
                album.copy(watchedAdCount = album.watchedAdCount + 1)
            } else {
                album
            }
        }
        saveWatchedAdCounts()
    }
    
    /**
     * Get album by ID
     */
    fun getAlbumById(albumId: String): Album? {
        return _albums.value.find { it.id == albumId }
    }
    
    /**
     * Get album by album_id (for character mapping)
     */
    fun getAlbumByAlbumId(albumId: String): Album? {
        return _albums.value.find { it.albumId == albumId }
    }
    
    companion object {
        @Volatile
        private var instance: AlbumRepository? = null
        
        fun getInstance(context: Context): AlbumRepository {
            return instance ?: synchronized(this) {
                instance ?: AlbumRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

