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

    // Favourites
    val FAVOURITE_CHARACTER_IDS = stringPreferencesKey("favourite_character_ids")
    val FAVOURITE_PHOTO_URLS = stringPreferencesKey("favourite_photo_urls")

    // Add more keys as needed
}

