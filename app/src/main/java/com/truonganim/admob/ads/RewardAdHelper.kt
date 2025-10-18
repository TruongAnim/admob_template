package com.truonganim.admob.ads

import android.content.Context
import android.widget.Toast

/**
 * Reward Ad Helper
 * Helper functions to request reward ads from UI
 */
object RewardAdHelper {
    
    /**
     * Request reward ad for a placement
     * 
     * @param context Context
     * @param place Reward ad placement
     * @param onRewardEarned Callback when user earns reward (amount)
     * @param onAdClosed Callback when ad is closed (regardless of reward)
     */
    fun requestRewardAd(
        context: Context,
        place: RewardAdPlace,
        onRewardEarned: (Int) -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        val manager = RewardAdManager.getInstance(context)
        
        val errorMessage = manager.requestRewardAd(
            place = place,
            onRewardEarned = onRewardEarned,
            onAdClosed = onAdClosed
        )
        
        // Show error toast if any
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}

