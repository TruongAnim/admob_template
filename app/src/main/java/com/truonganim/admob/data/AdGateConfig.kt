package com.truonganim.admob.data

import org.json.JSONObject

/**
 * Ad Gate Configuration
 */
data class AdGateConfig(
    val enabled: Boolean = false,
    val timeoutSeconds: Int = 10,
    val backgroundImageUrl: String = "",
    val adIntervalSeconds: Int = 30
) {
    companion object {
        /**
         * Parse from JSON string
         */
        fun fromJson(jsonString: String): AdGateConfig {
            return try {
                val json = JSONObject(jsonString)
                AdGateConfig(
                    enabled = json.optBoolean("enabled", false),
                    timeoutSeconds = json.optInt("timeout_seconds", 10),
                    backgroundImageUrl = json.optString("background_image_url", ""),
                    adIntervalSeconds = json.optInt("ad_interval_seconds", 30)
                )
            } catch (e: Exception) {
                // Return default config if parsing fails
                AdGateConfig()
            }
        }
        
        /**
         * Default config for testing
         */
        fun getDefault(): AdGateConfig {
            return AdGateConfig(
                enabled = true,
                timeoutSeconds = 10,
                backgroundImageUrl = "https://picsum.photos/1080/1920?random=2",
                adIntervalSeconds = 30
            )
        }
    }
}

