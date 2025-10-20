package com.truonganim.admob.ui.components

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App Loading Overlay Manager
 * Manages global loading overlay state
 */
class AppLoadingOverlayManager private constructor() {
    
    companion object {
        @Volatile
        private var instance: AppLoadingOverlayManager? = null
        
        fun getInstance(): AppLoadingOverlayManager {
            return instance ?: synchronized(this) {
                instance ?: AppLoadingOverlayManager().also {
                    instance = it
                }
            }
        }
    }
    
    private val _loadingState = MutableStateFlow<AppLoadingState?>(null)
    val loadingState: StateFlow<AppLoadingState?> = _loadingState.asStateFlow()
    
    private var isCancelled = false
    
    /**
     * Show loading overlay
     */
    fun show(message: String = "Loading...") {
        isCancelled = false
        _loadingState.value = AppLoadingState(
            message = message,
            isVisible = true
        )
    }
    
    /**
     * Update loading message
     */
    fun updateMessage(message: String) {
        _loadingState.value?.let {
            _loadingState.value = it.copy(message = message)
        }
    }
    
    /**
     * Hide loading overlay
     */
    fun hide() {
        _loadingState.value = null
        isCancelled = false
    }
    
    /**
     * Cancel loading (user clicked cancel button)
     */
    fun cancel() {
        isCancelled = true
        hide()
    }
    
    /**
     * Check if loading was cancelled
     */
    fun isCancelled(): Boolean {
        return isCancelled
    }
    
    /**
     * Check if loading is currently showing
     */
    fun isShowing(): Boolean {
        return _loadingState.value?.isVisible == true
    }
}

/**
 * Loading state for UI
 */
data class AppLoadingState(
    val message: String,
    val isVisible: Boolean = true
)

