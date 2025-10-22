package com.truonganim.admob.billing

import android.content.Context
import com.truonganim.admob.ui.premium.PremiumActivity
import kotlinx.coroutines.flow.Flow

/**
 * Premium Helper
 * Utility class to easily access premium features
 */
object PremiumHelper {

    /**
     * Check if user is premium
     */
    suspend fun isPremium(context: Context): Boolean {
        return PremiumPreferencesManager.getInstance(context).getIsPremiumSync()
    }

    /**
     * Get premium status as Flow
     */
    fun isPremiumFlow(context: Context): Flow<Boolean> {
        return PremiumPreferencesManager.getInstance(context).isPremium
    }

    /**
     * Show premium screen
     */
    fun showPremiumScreen(context: Context) {
        PremiumActivity.start(context)
    }

    /**
     * Clear premium data (for testing)
     */
    suspend fun clearPremiumData(context: Context) {
        PremiumPreferencesManager.getInstance(context).clearPremiumData()
    }
}

