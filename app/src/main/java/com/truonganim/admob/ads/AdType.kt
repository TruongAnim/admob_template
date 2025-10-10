package com.truonganim.admob.ads

/**
 * Ad Type Enum
 * Defines different types of ads that can be shown
 */
enum class AdType(val value: String) {
    INTERSTITIAL("interstitial"),
    APP_OPEN("app_open"),
    NONE("none");
    
    companion object {
        fun fromString(value: String): AdType {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: INTERSTITIAL
        }
    }
}

