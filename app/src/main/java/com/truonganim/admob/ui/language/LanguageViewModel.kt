package com.truonganim.admob.ui.language

import androidx.lifecycle.ViewModel
import com.truonganim.admob.data.Language
import com.truonganim.admob.data.SupportedLanguages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Language Selection Screen
 * Manages selected language state
 */
class LanguageViewModel : ViewModel() {
    
    private val _selectedLanguage = MutableStateFlow<Language?>(null)
    val selectedLanguage: StateFlow<Language?> = _selectedLanguage.asStateFlow()
    
    private val _languages = MutableStateFlow(SupportedLanguages.languages)
    val languages: StateFlow<List<Language>> = _languages.asStateFlow()
    
    /**
     * Select a language
     */
    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
        println("🌍 Language selected: ${language.name} (${language.code})")
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

