package com.truonganim.admob.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore Preferences Keys
 * Centralized location for all preference keys
 */
object PreferencesKeys {
    
    // Onboarding
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    
    // Language
    val SELECTED_LANGUAGE_CODE = stringPreferencesKey("selected_language_code")
    
    // App Settings
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    val LAST_AD_SHOWN_TIME = longPreferencesKey("last_ad_shown_time")
    val AD_CLICK_COUNT = intPreferencesKey("ad_click_count")
    
    // Add more keys as needed
}

