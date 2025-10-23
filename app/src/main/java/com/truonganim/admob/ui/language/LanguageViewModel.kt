package com.truonganim.admob.ui.language

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Language
import com.truonganim.admob.data.SupportedLanguages
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.utils.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Language Selection Screen
 * Manages selected language state and loading state
 */
class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager.getInstance(application)

    private val _selectedLanguage = MutableStateFlow<Language?>(null)
    val selectedLanguage: StateFlow<Language?> = _selectedLanguage.asStateFlow()

    private val _languages = MutableStateFlow(SupportedLanguages.languages)
    val languages: StateFlow<List<Language>> = _languages.asStateFlow()

    // Loading state when user selects a language
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Track if this is the first language selection
    private val _isFirstSelection = MutableStateFlow(true)
    val isFirstSelection: StateFlow<Boolean> = _isFirstSelection.asStateFlow()

    init {
        loadCurrentLanguage()
    }

    /**
     * Load current language from preferences
     */
    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            val savedLanguageCode = preferencesManager.getValueSync(
                com.truonganim.admob.datastore.PreferencesKeys.SELECTED_LANGUAGE_CODE,
                ""
            )

            if (savedLanguageCode.isNotEmpty()) {
                // Find the language in supported languages
                val language = SupportedLanguages.languages.find { it.code == savedLanguageCode }
                if (language != null) {
                    _selectedLanguage.value = language
                    _isFirstSelection.value = false
                    println("🌍 Loaded saved language: ${language.name} (${language.code})")
                }
            } else {
                // No saved language, this is first selection
                _isFirstSelection.value = true
                println("🌍 No saved language, first selection")
            }
        }
    }

    /**
     * Select a language
     * Shows loading for 1 second
     */
    fun selectLanguage(language: Language) {
        viewModelScope.launch {
            _selectedLanguage.value = language
            println("🌍 Language selected: ${language.name} (${language.code})")

            // Show loading
            _isLoading.value = true
            println("⏳ Loading started...")

            // Wait 1 second
            delay(1000)

            // Hide loading
            _isLoading.value = false
            println("✅ Loading finished")
        }
    }

    /**
     * Confirm language selection
     * Saves to preferences
     * Locale will be applied automatically when activity recreates via BaseActivity.attachBaseContext()
     */
    fun confirmLanguage(): Language? {
        val selected = _selectedLanguage.value
        if (selected != null) {
            viewModelScope.launch {
                // Save to preferences
                preferencesManager.setSelectedLanguageCode(selected.code)
                println("💾 Saved language to preferences: ${selected.code}")

                // Mark as not first selection anymore
                _isFirstSelection.value = false
            }
        }
        return selected
    }

    /**
     * Check if a language is selected
     */
    fun isLanguageSelected(language: Language): Boolean {
        return _selectedLanguage.value?.code == language.code
    }
}

