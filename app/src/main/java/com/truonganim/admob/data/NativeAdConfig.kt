package com.truonganim.admob.data

import org.json.JSONObject

/**
 * Native Ad Configuration
 * Controls which native ad positions are enabled/disabled
 */
data class NativeAdConfig(
    val languageScreen: Boolean = true,
    val languageScreen2: Boolean = true,
    val onboardingPage1: Boolean = true,
    val onboardingPage3: Boolean = true,
    val homeScreen: Boolean = true,
    val listItem: Boolean = true
) {
    companion object {
        /**
         * Parse from JSON string
         * Expected JSON format:
         * {
         *   "language_screen": true,
         *   "language_screen_2": true,
         *   "onboarding_page_1": true,
         *   "onboarding_page_3": true,
         *   "home_screen": true,
         *   "list_item": true
         * }
         */
        fun fromJson(jsonString: String): NativeAdConfig {
            return try {
                val json = JSONObject(jsonString)
                NativeAdConfig(
                    languageScreen = json.optBoolean("language_screen", true),
                    languageScreen2 = json.optBoolean("language_screen_2", true),
                    onboardingPage1 = json.optBoolean("onboarding_page_1", true),
                    onboardingPage3 = json.optBoolean("onboarding_page_3", true),
                    homeScreen = json.optBoolean("home_screen", true),
                    listItem = json.optBoolean("list_item", true)
                )
            } catch (e: Exception) {
                println("❌ Failed to parse NativeAdConfig: ${e.message}")
                // Return default config if parsing fails (all enabled)
                NativeAdConfig()
            }
        }
        
        /**
         * Default config - all positions enabled
         */
        fun getDefault(): NativeAdConfig {
            return NativeAdConfig(
                languageScreen = true,
                languageScreen2 = true,
                onboardingPage1 = true,
                onboardingPage3 = true,
                homeScreen = true,
                listItem = true
            )
        }
        
        /**
         * Example config for testing - some positions disabled
         */
        fun getTestConfig(): NativeAdConfig {
            return NativeAdConfig(
                languageScreen = true,
                languageScreen2 = false,
                onboardingPage1 = true,
                onboardingPage3 = false,
                homeScreen = true,
                listItem = false
            )
        }
    }
    
    /**
     * Convert to JSON string for Firebase Remote Config
     */
    fun toJson(): String {
        return JSONObject().apply {
            put("language_screen", languageScreen)
            put("language_screen_2", languageScreen2)
            put("onboarding_page_1", onboardingPage1)
            put("onboarding_page_3", onboardingPage3)
            put("home_screen", homeScreen)
            put("list_item", listItem)
        }.toString()
    }
}

