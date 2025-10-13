package com.truonganim.admob.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Remote Config Helper
 * Simplifies working with Firebase Remote Config
 */
class RemoteConfigHelper {
    
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    
    init {
        // Configure Remote Config settings
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour in production
            // For testing, you can set it to 0: minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Set default values
        setDefaultValues()
    }
    
    /**
     * Set default values for Remote Config
     * These values will be used if fetch fails or before first fetch
     */
    private fun setDefaultValues() {
        val defaults = hashMapOf<String, Any>(
            RemoteConfigKeys.TEST_MESSAGE to "Hello from default config!",
            RemoteConfigKeys.IS_FEATURE_ENABLED to false,
            RemoteConfigKeys.APP_VERSION_MIN to "1.0.0",
            RemoteConfigKeys.FORCE_UPDATE to false,
            RemoteConfigKeys.SPLASH_AD_TYPE to "interstitial", // Default: interstitial
            RemoteConfigKeys.SHOW_ADS to true,
            RemoteConfigKeys.AD_INTERVAL to 30,
            RemoteConfigKeys.ALBUM_DATA to "[]" // Empty array as default
        )
        remoteConfig.setDefaultsAsync(defaults)
    }
    
    /**
     * Fetch and activate Remote Config values
     * @return true if fetch and activate successful, false otherwise
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            val result = remoteConfig.fetchAndActivate().await()
            println("🔥 Firebase Remote Config fetch and activate: $result")
            result
        } catch (e: Exception) {
            println("❌ Firebase Remote Config fetch failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get string value from Remote Config
     */
    fun getString(key: String): String {
        return remoteConfig.getString(key)
    }
    
    /**
     * Get boolean value from Remote Config
     */
    fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }
    
    /**
     * Get long value from Remote Config
     */
    fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }
    
    /**
     * Get double value from Remote Config
     */
    fun getDouble(key: String): Double {
        return remoteConfig.getDouble(key)
    }
    
    /**
     * Print all current config values (for debugging)
     */
    fun printAllConfigs() {
        println("📋 Current Remote Config Values:")
        println("  - ${RemoteConfigKeys.TEST_MESSAGE}: ${getString(RemoteConfigKeys.TEST_MESSAGE)}")
        println("  - ${RemoteConfigKeys.IS_FEATURE_ENABLED}: ${getBoolean(RemoteConfigKeys.IS_FEATURE_ENABLED)}")
        println("  - ${RemoteConfigKeys.APP_VERSION_MIN}: ${getString(RemoteConfigKeys.APP_VERSION_MIN)}")
        println("  - ${RemoteConfigKeys.FORCE_UPDATE}: ${getBoolean(RemoteConfigKeys.FORCE_UPDATE)}")
        println("  - ${RemoteConfigKeys.SPLASH_AD_TYPE}: ${getString(RemoteConfigKeys.SPLASH_AD_TYPE)}")
        println("  - ${RemoteConfigKeys.SHOW_ADS}: ${getBoolean(RemoteConfigKeys.SHOW_ADS)}")
        println("  - ${RemoteConfigKeys.AD_INTERVAL}: ${getLong(RemoteConfigKeys.AD_INTERVAL)}")
    }
    
    companion object {
        @Volatile
        private var instance: RemoteConfigHelper? = null
        
        fun getInstance(): RemoteConfigHelper {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigHelper().also { instance = it }
            }
        }
    }
}

