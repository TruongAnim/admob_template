package com.truonganim.admob.data

import com.truonganim.admob.BuildConfig
import org.json.JSONObject

/**
 * App Open Ad Configuration
 */
data class AppOpenAdConfig(
    val enabled: Boolean = false,
    val adUnitId: String = "",
    val timeoutSeconds: Int = 10,
    val backgroundImageUrl: String = ""
) {
    companion object {
        /**
         * Parse from JSON string
         */
        fun fromJson(jsonString: String): AppOpenAdConfig {
            return try {
                val json = JSONObject(jsonString)
                AppOpenAdConfig(
                    enabled = json.optBoolean("enabled", false),
                    adUnitId = BuildConfig.RESUME_APP_OPEN_AD_ID,
                    timeoutSeconds = json.optInt("timeout_seconds", 10),
                    backgroundImageUrl = json.optString("background_image_url", "")
                )
            } catch (e: Exception) {
                // Return default config if parsing fails
                AppOpenAdConfig()
            }
        }
        
        /**
         * Default config for testing
         */
        fun getDefault(): AppOpenAdConfig {
            return AppOpenAdConfig(
                enabled = true,
                adUnitId = BuildConfig.RESUME_APP_OPEN_AD_ID,
                timeoutSeconds = 10,
                backgroundImageUrl = "https://picsum.photos/1080/1920?random=1"
            )
        }
    }
}

