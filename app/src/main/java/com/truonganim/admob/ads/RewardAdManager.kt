package com.truonganim.admob.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.truonganim.admob.utils.AdLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reward Ad Manager
 * Manages multiple reward ad placements independently
 */
class RewardAdManager private constructor(
    private val context: Context
) {
    // State for each placement
    private val adStates = mutableMapOf<RewardAdPlace, RewardAdState>()
    
    // Current loading state (for UI)
    private val _loadingState = MutableStateFlow<RewardAdLoadingState?>(null)
    val loadingState: StateFlow<RewardAdLoadingState?> = _loadingState.asStateFlow()
    
    companion object {
        private const val TAG = "RewardAdManager"
        
        @Volatile
        private var instance: RewardAdManager? = null
        
        fun getInstance(context: Context): RewardAdManager {
            return instance ?: synchronized(this) {
                instance ?: RewardAdManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        // Initialize state for all placements
        RewardAdPlace.values().forEach { place ->
            adStates[place] = RewardAdState(place)
        }
    }
    
    /**
     * Request reward ad for a placement
     * Returns error message if cannot request, null if request started
     */
    fun requestRewardAd(
        place: RewardAdPlace,
        onRewardEarned: (Int) -> Unit,
        onAdClosed: () -> Unit
    ): String? {
        val state = adStates[place] ?: return "Invalid placement"
        
        // Check if already have ad
        if (state.isAdAvailable()) {
            AdLogger.i(TAG, "Ad already available for ${place.placeName}, showing immediately")
            showRewardAd(place, onRewardEarned, onAdClosed)
            return null // Will show immediately
        }
        
        // Check interval
        if (!state.canRequestAd()) {
            val remaining = state.getRemainingSeconds()
            val message = "Server đang quá tải, vui lòng thử lại sau $remaining giây"
            AdLogger.w(TAG, "Cannot request ad for ${place.placeName}: interval not reached ($remaining seconds remaining)")
            return message
        }
        
        // Check if already loading
        if (state.isLoading) {
            AdLogger.d(TAG, "Ad already loading for ${place.placeName}")
            _loadingState.value = RewardAdLoadingState(
                place = place,
                status = LoadingStatus.LOADING,
                message = "Loading ad..."
            )
            return null // Already loading
        }
        
        // Start loading
        loadRewardAd(place, onRewardEarned, onAdClosed)
        return null
    }
    
    /**
     * Load reward ad for a placement
     */
    private fun loadRewardAd(
        place: RewardAdPlace,
        onRewardEarned: (Int) -> Unit,
        onAdClosed: () -> Unit
    ) {
        val state = adStates[place] ?: return
        
        state.isLoading = true
        state.lastLoadTime = System.currentTimeMillis()
        state.lastError = null
        
        _loadingState.value = RewardAdLoadingState(
            place = place,
            status = LoadingStatus.LOADING,
            message = "Loading ${place.placeName} ad..."
        )
        
        AdLogger.i(TAG, "Loading reward ad for ${place.placeName}")
        
        val request = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            place.adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    AdLogger.i(TAG, "Reward ad loaded for ${place.placeName}")
                    state.rewardedAd = ad
                    state.isLoading = false

                    // Show ad immediately
                    showRewardAd(place, onRewardEarned, onAdClosed)
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val errorMsg = "Load ad lỗi: ${loadAdError.message}"
                    AdLogger.e(TAG, "Reward ad failed to load for ${place.placeName}: ${loadAdError.message}")
                    state.isLoading = false
                    state.lastError = errorMsg
                    
                    _loadingState.value = RewardAdLoadingState(
                        place = place,
                        status = LoadingStatus.ERROR,
                        message = errorMsg
                    )
                }
            }
        )
    }
    
    /**
     * Show reward ad
     */
    private fun showRewardAd(
        place: RewardAdPlace,
        onRewardEarned: (Int) -> Unit,
        onAdClosed: () -> Unit
    ) {
        val state = adStates[place] ?: return
        val ad = state.rewardedAd ?: return

        AdLogger.i(TAG, "Preparing to show reward ad for ${place.placeName}")

        // Need activity context to show ad
        // Will be called from UI with activity
        _loadingState.value = RewardAdLoadingState(
            place = place,
            status = LoadingStatus.SHOWING,
            message = "Showing ad...",
            onShow = { activity ->
                AdLogger.i(TAG, "onShow callback invoked for ${place.placeName}")
                var rewardEarned = false
                
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        AdLogger.i(TAG, "Reward ad dismissed for ${place.placeName}")
                        state.rewardedAd = null
                        
                        _loadingState.value = null
                        
                        if (rewardEarned) {
                            onRewardEarned(1) // Reward amount
                        }
                        onAdClosed()
                    }
                    
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AdLogger.e(TAG, "Reward ad failed to show for ${place.placeName}: ${adError.message}")
                        state.rewardedAd = null
                        
                        _loadingState.value = RewardAdLoadingState(
                            place = place,
                            status = LoadingStatus.ERROR,
                            message = "Failed to show ad: ${adError.message}"
                        )
                    }
                    
                    override fun onAdShowedFullScreenContent() {
                        AdLogger.i(TAG, "Reward ad showed for ${place.placeName}")
                    }
                }
                
                ad.show(activity) { rewardItem ->
                    val amount = rewardItem.amount
                    AdLogger.i(TAG, "User earned reward: $amount for ${place.placeName}")
                    rewardEarned = true
                }
            }
        )
    }
    
    /**
     * Cancel loading (user dismissed dialog)
     */
    fun cancelLoading() {
        AdLogger.d(TAG, "Loading cancelled by user")
        _loadingState.value = null
    }
    
    /**
     * Get state for a placement
     */
    fun getState(place: RewardAdPlace): RewardAdState? {
        return adStates[place]
    }
}

/**
 * Loading state for UI
 */
data class RewardAdLoadingState(
    val place: RewardAdPlace,
    val status: LoadingStatus,
    val message: String,
    val onShow: ((Activity) -> Unit)? = null
)

enum class LoadingStatus {
    LOADING,
    READY,
    SHOWING,
    ERROR
}

