package com.truonganim.admob.ads

import com.truonganim.admob.BuildConfig

/**
 * Reward Ad Placement
 * Define all reward ad placements in the app
 */
enum class RewardAdPlace(
    val placeName: String,
    val adUnitId: String
) {
    // Example placements - add more as needed
    UNLOCK_CHARACTER(
        placeName = "Unlock Character",
        adUnitId = BuildConfig.REWARD_AD_UNLOCK_CHARACTER
    ),
    
    UNLOCK_PHOTO(
        placeName = "Unlock Photo",
        adUnitId = BuildConfig.REWARD_AD_UNLOCK_PHOTO
    ),
    
    DAILY_BONUS(
        placeName = "Daily Bonus",
        adUnitId = BuildConfig.REWARD_AD_DAILY_BONUS
    ),
    
    EXTRA_COINS(
        placeName = "Extra Coins",
        adUnitId = BuildConfig.REWARD_AD_EXTRA_COINS
    );
    
    companion object {
        // Interval between ad requests for same placement (15 seconds)
        const val AD_REQUEST_INTERVAL_SECONDS = 15
    }
}

