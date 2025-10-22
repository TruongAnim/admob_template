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

    // Albums
    val ALBUM_WATCHED_AD_COUNTS = stringPreferencesKey("album_watched_ad_counts")

    // Character Unlock
    val UNLOCKED_CHARACTER_IDS = stringPreferencesKey("unlocked_character_ids")
    val CHARACTER_AD_PROGRESS = stringPreferencesKey("character_ad_progress") // JSON: {"characterId": watchedCount}

    // Notification Permission
    val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    val NOTIFICATION_PERMISSION_DIALOG_SHOWN = booleanPreferencesKey("notification_permission_dialog_shown")

    // Premium / IAP
    val IS_PREMIUM = booleanPreferencesKey("is_premium")
    val PREMIUM_PURCHASE_TOKEN = stringPreferencesKey("premium_purchase_token")
    val PREMIUM_PRODUCT_ID = stringPreferencesKey("premium_product_id")
    val PREMIUM_PURCHASE_TIME = longPreferencesKey("premium_purchase_time")

    // Add more keys as needed
}

