package com.truonganim.admob.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Splash Screen
 * Manages loading progress and completion state
 * Fetches Firebase Remote Config during splash
 */
class SplashViewModel : ViewModel() {

    private val remoteConfigHelper = RemoteConfigHelper.getInstance()
    
    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()
    
    private val _isLoadingComplete = MutableStateFlow(false)
    val isLoadingComplete: StateFlow<Boolean> = _isLoadingComplete.asStateFlow()
    
    init {
        startLoading()
    }
    
    /**
     * Simulates loading process with progress updates
     * Also fetches Firebase Remote Config
     */
    private fun startLoading() {
        viewModelScope.launch {
            // Phase 1: Simulate initial loading (0-50%)
            for (progress in 0..50 step 5) {
                _loadingProgress.value = progress
                delay(50)
            }

            // Phase 2: Fetch Remote Config (50-80%)
            println("🔄 Fetching Firebase Remote Config...")
            val fetchSuccess = remoteConfigHelper.fetchAndActivate()

            if (fetchSuccess) {
                println("✅ Remote Config fetched successfully!")
            } else {
                println("⚠️ Using default Remote Config values")
            }

            _loadingProgress.value = 80

            // Phase 3: Test Remote Config values (80-100%)
            testRemoteConfig()

            for (progress in 80..100 step 5) {
                _loadingProgress.value = progress
                delay(50)
            }

            // Mark loading as complete
            _isLoadingComplete.value = true
            println("✅ Splash loading completed successfully!")
        }
    }

    /**
     * Test Remote Config by printing values
     */
    private fun testRemoteConfig() {
        println("\n" + "=".repeat(50))
        println("🧪 Testing Firebase Remote Config")
        println("=".repeat(50))

        val testMessage = remoteConfigHelper.getString(RemoteConfigKeys.TEST_MESSAGE)
        val isFeatureEnabled = remoteConfigHelper.getBoolean(RemoteConfigKeys.IS_FEATURE_ENABLED)

        println("📝 Test Message: $testMessage")
        println("🎯 Feature Enabled: $isFeatureEnabled")

        // Print all configs
        remoteConfigHelper.printAllConfigs()

        println("=".repeat(50) + "\n")
    }
}

