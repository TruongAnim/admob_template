package com.truonganim.admob.ads

import com.google.android.gms.ads.rewarded.RewardedAd

/**
 * Reward Ad State for each placement
 */
data class RewardAdState(
    val place: RewardAdPlace,
    var rewardedAd: RewardedAd? = null,
    var isLoading: Boolean = false,
    var lastLoadTime: Long = 0L,
    var lastError: String? = null
) {
    /**
     * Check if can request ad (respect interval)
     */
    fun canRequestAd(): Boolean {
        val now = System.currentTimeMillis()
        val elapsed = (now - lastLoadTime) / 1000 // seconds
        return elapsed >= RewardAdPlace.AD_REQUEST_INTERVAL_SECONDS
    }
    
    /**
     * Get remaining seconds until can request again
     */
    fun getRemainingSeconds(): Int {
        val now = System.currentTimeMillis()
        val elapsed = (now - lastLoadTime) / 1000 // seconds
        val remaining = RewardAdPlace.AD_REQUEST_INTERVAL_SECONDS - elapsed.toInt()
        return maxOf(0, remaining)
    }
    
    /**
     * Check if ad is available
     */
    fun isAdAvailable(): Boolean {
        return rewardedAd != null && !isLoading
    }
}

