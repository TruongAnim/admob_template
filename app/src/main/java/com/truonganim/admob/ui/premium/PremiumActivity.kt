package com.truonganim.admob.ui.premium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.truonganim.admob.MainActivity
import com.truonganim.admob.billing.BillingState
import com.truonganim.admob.billing.PremiumPreferencesManager
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Activity
 * Standalone activity for IAP subscription
 * Can be started from anywhere in the app
 */
class PremiumActivity : BaseActivity() {

    private lateinit var viewModel: PremiumViewModel
    private lateinit var premiumPrefs: PremiumPreferencesManager

    companion object {
        /**
         * Create intent to start PremiumActivity
         * Easy to use from anywhere
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, PremiumActivity::class.java)
        }

        /**
         * Start PremiumActivity
         */
        fun start(context: Context) {
            context.startActivity(createIntent(context))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[PremiumViewModel::class.java]
        premiumPrefs = PremiumPreferencesManager.getInstance(this)

        // Observe premium status changes
        observePremiumStatus()

        setContent {
            AdMobBaseTheme {
                PremiumScreen(
                    viewModel = viewModel,
                    onClose = {
                        finish()
                    },
                    onPurchaseSuccess = {
                        handlePurchaseSuccess()
                    },
                    onContinueClick = {
                        purchaseSelectedPlan()
                    },
                    modifier = androidx.compose.ui.Modifier
                )
            }
        }
    }

    /**
     * Observe premium status changes
     * When user becomes premium, navigate to home
     */
    private fun observePremiumStatus() {
        lifecycleScope.launch {
            premiumPrefs.isPremium.collect { isPremium ->
                if (isPremium) {
                    // User is now premium, navigate to home
                    handlePurchaseSuccess()
                }
            }
        }
    }

    /**
     * Handle purchase success
     * Close all screens and restart MainActivity
     */
    private fun handlePurchaseSuccess() {
        lifecycleScope.launch {
            // Small delay to show success state
            delay(500)

            // Clear all activities and start MainActivity fresh
            val intent = Intent(this@PremiumActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Trigger purchase flow
     * Called from the screen when user clicks continue
     */
    fun purchaseSelectedPlan() {
        viewModel.purchaseSelectedPlan(this)
    }

    override fun onResume() {
        super.onResume()
        // Check if user is already premium
        viewModel.checkPremiumStatus {
            handlePurchaseSuccess()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Allow back press to close the screen
        super.onBackPressed()
    }
}

