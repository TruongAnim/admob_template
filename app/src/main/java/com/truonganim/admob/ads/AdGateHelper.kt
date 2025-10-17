package com.truonganim.admob.ads

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * Ad Gate Helper
 * Helper class to simplify showing ad gates
 */
object AdGateHelper {
    
    /**
     * Show ad gate (optional - respects interval)
     */
    fun showOptionalAdGate(
        launcher: ActivityResultLauncher<Intent>,
        activity: Activity
    ) {
        val intent = AdGateActivity.createIntent(activity, isRequired = false)
        launcher.launch(intent)
    }
    
    /**
     * Show ad gate (required - ignores interval)
     */
    fun showRequiredAdGate(
        launcher: ActivityResultLauncher<Intent>,
        activity: Activity
    ) {
        val intent = AdGateActivity.createIntent(activity, isRequired = true)
        launcher.launch(intent)
    }
    
    /**
     * Handle ad gate result
     */
    fun handleAdGateResult(
        resultCode: Int,
        data: Intent?,
        onAdShown: () -> Unit,
        onAdFailed: () -> Unit,
        onAdSkipped: () -> Unit
    ) {
        when (resultCode) {
            AdGateActivity.RESULT_AD_SHOWN -> {
                // Ad was shown successfully
                onAdShown()
            }
            AdGateActivity.RESULT_AD_FAILED -> {
                // Ad failed to load/show (for required ads)
                onAdFailed()
            }
            AdGateActivity.RESULT_AD_SKIPPED -> {
                // Ad was skipped (interval not reached or optional ad failed)
                onAdSkipped()
            }
        }
    }
}

