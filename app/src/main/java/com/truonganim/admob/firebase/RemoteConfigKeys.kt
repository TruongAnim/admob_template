package com.truonganim.admob.firebase

/**
 * Remote Config Keys
 * Centralized place to manage all Firebase Remote Config keys
 */
object RemoteConfigKeys {
    
    // Test config
    const val TEST_MESSAGE = "test_message"
    const val IS_FEATURE_ENABLED = "is_feature_enabled"
    
    // App configs
    const val APP_VERSION_MIN = "app_version_min"
    const val FORCE_UPDATE = "force_update"
    
    // Ad configs
    const val SPLASH_AD_TYPE = "splash_ad_type" // Values: "interstitial", "app_open", "none"
    const val SHOW_ADS = "show_ads"
    const val AD_INTERVAL = "ad_interval"

    // App Open Ad config (JSON format)
    const val APP_OPEN_AD_CONFIG = "app_open_ad_config"

    // Ad Gate config (JSON format)
    const val AD_GATE_CONFIG = "ad_gate_config"

    // Album data (character data - legacy name)
    const val ALBUM_DATA = "album_data"

    // Albums data (new album list)
    const val ALBUMS_DATA = "albums_data"
}

