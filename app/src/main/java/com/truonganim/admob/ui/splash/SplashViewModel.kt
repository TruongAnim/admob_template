package com.truonganim.admob.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Splash Screen
 * Manages loading progress and completion state
 */
class SplashViewModel : ViewModel() {
    
    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()
    
    private val _isLoadingComplete = MutableStateFlow(false)
    val isLoadingComplete: StateFlow<Boolean> = _isLoadingComplete.asStateFlow()
    
    init {
        startLoading()
    }
    
    /**
     * Simulates loading process with progress updates
     */
    private fun startLoading() {
        viewModelScope.launch {
            // Simulate loading from 0% to 100%
            for (progress in 0..100 step 5) {
                _loadingProgress.value = progress
                delay(100) // Delay 100ms for each step
            }
            
            // Mark loading as complete
            _isLoadingComplete.value = true
            println("✅ Splash loading completed successfully!")
        }
    }
}

