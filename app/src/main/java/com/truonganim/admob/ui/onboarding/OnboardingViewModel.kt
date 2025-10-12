package com.truonganim.admob.ui.onboarding

import androidx.lifecycle.ViewModel
import com.truonganim.admob.data.OnboardingPage
import com.truonganim.admob.data.OnboardingPages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Onboarding Screen
 * Manages current page state
 */
class OnboardingViewModel : ViewModel() {
    
    private val _pages = MutableStateFlow(OnboardingPages.pages)
    val pages: StateFlow<List<OnboardingPage>> = _pages.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    /**
     * Update current page
     */
    fun updateCurrentPage(page: Int) {
        _currentPage.value = page
        println("📄 Onboarding page changed to: $page")
    }
    
    /**
     * Check if current page is the last page
     */
    fun isLastPage(): Boolean {
        return _currentPage.value == _pages.value.size - 1
    }
    
    /**
     * Get total number of pages
     */
    fun getTotalPages(): Int {
        return _pages.value.size
    }
}

