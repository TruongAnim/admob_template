package com.truonganim.admob.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper class for managing app locale
 */
object LocaleHelper {

    /**
     * Apply locale to the app using AppCompatDelegate
     * This is the recommended way for Android 13+ (API 33+)
     */
    fun applyLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
        println("🌍 Applied locale: $languageCode")
    }

    /**
     * Get current app locale
     */
    fun getCurrentLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) {
                Locale.getDefault().language
            } else {
                locales[0]?.toLanguageTag() ?: Locale.getDefault().language
            }
        } else {
            // Older Android versions
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) {
                Locale.getDefault().language
            } else {
                locales[0]?.language ?: Locale.getDefault().language
            }
        }
    }

    /**
     * Restart activity to apply locale change
     */
    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

