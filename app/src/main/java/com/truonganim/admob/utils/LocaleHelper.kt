package com.truonganim.admob.utils

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.truonganim.admob.datastore.PreferencesKeys
import com.truonganim.admob.datastore.PreferencesManager
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Helper class for managing app locale
 */
object LocaleHelper {

    /**
     * Wrap context with locale for activities
     * This is called in attachBaseContext() of BaseActivity
     */
    fun wrapContext(context: Context): Context {
        val languageCode = runBlocking {
            getSavedLanguage(context)
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        updateLocaleConfiguration(config, locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Update locale configuration for all Android versions
     */
    private fun updateLocaleConfiguration(configuration: Configuration, newLocale: Locale) {
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)

        val localeList = LocaleList(newLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
    }

    /**
     * Get saved language from preferences
     */
    suspend fun getSavedLanguage(context: Context): String {
        val preferencesManager = PreferencesManager.getInstance(context)
        return preferencesManager.getValueSync(PreferencesKeys.SELECTED_LANGUAGE_CODE, "en")
    }
}

