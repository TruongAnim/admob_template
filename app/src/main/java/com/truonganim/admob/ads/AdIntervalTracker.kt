package com.truonganim.admob.ads

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Ad Interval Tracker
 * Tracks the last time an ad was shown to enforce ad intervals
 */
class AdIntervalTracker private constructor(
    private val context: Context
) {
    companion object {
        private const val DATASTORE_NAME = "ad_interval_tracker"
        private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
        
        private val LAST_AD_SHOWN_TIME_KEY = longPreferencesKey("last_ad_shown_time")
        
        @Volatile
        private var instance: AdIntervalTracker? = null
        
        fun getInstance(context: Context): AdIntervalTracker {
            return instance ?: synchronized(this) {
                instance ?: AdIntervalTracker(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Check if enough time has passed since last ad
     */
    suspend fun shouldShowAd(intervalSeconds: Int): Boolean {
        val lastAdTime = getLastAdShownTime()
        val currentTime = System.currentTimeMillis()
        val intervalMillis = intervalSeconds * 1000L
        
        return (currentTime - lastAdTime) >= intervalMillis
    }
    
    /**
     * Record that an ad was shown
     */
    suspend fun recordAdShown() {
        context.dataStore.edit { preferences ->
            preferences[LAST_AD_SHOWN_TIME_KEY] = System.currentTimeMillis()
        }
    }
    
    /**
     * Get last ad shown time
     */
    private suspend fun getLastAdShownTime(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_AD_SHOWN_TIME_KEY] ?: 0L
        }.first()
    }
    
    /**
     * Reset tracker (for testing)
     */
    suspend fun reset() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_AD_SHOWN_TIME_KEY)
        }
    }
}

