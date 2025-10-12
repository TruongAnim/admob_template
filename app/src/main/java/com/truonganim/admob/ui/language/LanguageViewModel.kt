package com.truonganim.admob.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.data.Language
import com.truonganim.admob.data.SupportedLanguages
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Language Selection Screen
 * Manages selected language state and loading state
 */
class LanguageViewModel : ViewModel() {

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

            // Mark as not first selection anymore
            _isFirstSelection.value = false
        }
    }
    
    /**
     * Confirm language selection
     */
    fun confirmLanguage(): Language? {
        val selected = _selectedLanguage.value
        if (selected != null) {
            println("✅ Language confirmed: ${selected.name} (${selected.code})")
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

