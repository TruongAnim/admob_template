package com.truonganim.admob.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Billing Repository
 * Handles all Google Play Billing operations
 * Singleton pattern for easy access
 */
class BillingRepository private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val premiumPrefs = PremiumPreferencesManager.getInstance(context)

    // Billing client
    private var billingClient: BillingClient? = null

    // Available products
    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    // Billing state
    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    companion object {
        @Volatile
        private var instance: BillingRepository? = null

        fun getInstance(context: Context): BillingRepository {
            return instance ?: synchronized(this) {
                instance ?: BillingRepository(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }

    init {
        initializeBillingClient()
    }

    /**
     * Initialize billing client
     */
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    handlePurchases(purchases)
                }
            }
            .enablePendingPurchases()
            .build()

        connectToBillingService()
    }

    /**
     * Connect to billing service
     */
    private fun connectToBillingService() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("✅ Billing client connected")
                    scope.launch {
                        queryProducts()
                        syncPurchases()
                    }
                } else {
                    println("❌ Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                println("⚠️ Billing service disconnected")
                // Try to reconnect
                connectToBillingService()
            }
        })
    }

    /**
     * Query available subscription products
     */
    private suspend fun queryProducts() = withContext(Dispatchers.IO) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionPlans.WEEKLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionPlans.MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _availableProducts.value = productDetailsList
                println("✅ Products loaded: ${productDetailsList.size}")
            } else {
                println("❌ Failed to load products: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Sync purchases with Google Play
     * Check if user has active subscription
     */
    suspend fun syncPurchases() = withContext(Dispatchers.IO) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    if (purchases.isNotEmpty()) {
                        handlePurchases(purchases)
                    } else {
                        // No active subscriptions
                        premiumPrefs.setIsPremium(false)
                        println("ℹ️ No active subscriptions found")
                    }
                }
            } else {
                println("❌ Failed to sync purchases: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Handle purchases
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        scope.launch {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Verify purchase
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    
                    // Save premium status
                    val productId = purchase.products.firstOrNull() ?: ""
                    premiumPrefs.savePurchaseDetails(
                        productId = productId,
                        purchaseToken = purchase.purchaseToken,
                        purchaseTime = purchase.purchaseTime
                    )
                    println("✅ Premium activated: $productId")
                }
            }
        }
    }

    /**
     * Acknowledge purchase
     */
    private suspend fun acknowledgePurchase(purchase: Purchase) = withContext(Dispatchers.IO) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                println("✅ Purchase acknowledged")
            } else {
                println("❌ Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch purchase flow
     */
    suspend fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails
    ): BillingResult = suspendCancellableCoroutine { continuation ->
        _billingState.value = BillingState.Loading

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            _billingState.value = BillingState.Error("No offer available")
            continuation.resume(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                    .build()
            )
            return@suspendCancellableCoroutine
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState.Success
        } else {
            _billingState.value = BillingState.Error(billingResult?.debugMessage ?: "Unknown error")
        }

        continuation.resume(billingResult ?: BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.ERROR)
            .build()
        )
    }

    /**
     * Reset billing state
     */
    fun resetBillingState() {
        _billingState.value = BillingState.Idle
    }

    /**
     * Disconnect billing client
     */
    fun disconnect() {
        billingClient?.endConnection()
    }
}

/**
 * Billing State
 */
sealed class BillingState {
    object Idle : BillingState()
    object Loading : BillingState()
    object Success : BillingState()
    data class Error(val message: String) : BillingState()
}

