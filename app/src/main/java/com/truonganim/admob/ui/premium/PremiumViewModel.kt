package com.truonganim.admob.ui.premium

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.truonganim.admob.billing.BillingRepository
import com.truonganim.admob.billing.BillingState
import com.truonganim.admob.billing.PremiumPreferencesManager
import com.truonganim.admob.billing.SubscriptionPlan
import com.truonganim.admob.billing.SubscriptionPlans
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Premium ViewModel
 * Manages premium screen state and billing operations
 */
class PremiumViewModel(application: Application) : AndroidViewModel(application) {

    private val billingRepository = BillingRepository.getInstance(application)
    private val premiumPrefs = PremiumPreferencesManager.getInstance(application)

    // UI State
    private val _uiState = MutableStateFlow<PremiumUiState>(PremiumUiState.Loading)
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    // Selected plan
    private val _selectedPlan = MutableStateFlow<SubscriptionPlan?>(SubscriptionPlans.MONTHLY)
    val selectedPlan: StateFlow<SubscriptionPlan?> = _selectedPlan.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Available products from Google Play
    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())

    init {
        loadProducts()
        observeBillingState()
    }

    /**
     * Load available products
     */
    private fun loadProducts() {
        viewModelScope.launch {
            billingRepository.availableProducts.collect { products ->
                _availableProducts.value = products
                if (products.isNotEmpty()) {
                    _uiState.value = PremiumUiState.Success(getSubscriptionPlans(products))
                } else {
                    // Use default plans if products not loaded yet
                    _uiState.value = PremiumUiState.Success(SubscriptionPlans.getAll())
                }
            }
        }
    }

    /**
     * Observe billing state
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            billingRepository.billingState.collect { state ->
                when (state) {
                    is BillingState.Loading -> {
                        _isLoading.value = true
                    }
                    is BillingState.Success -> {
                        _isLoading.value = false
                        // Purchase successful - will be handled by repository
                    }
                    is BillingState.Error -> {
                        _isLoading.value = false
                        _uiState.value = PremiumUiState.Error(state.message)
                    }
                    is BillingState.Idle -> {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    /**
     * Convert ProductDetails to SubscriptionPlan
     */
    private fun getSubscriptionPlans(products: List<ProductDetails>): List<SubscriptionPlan> {
        return products.mapNotNull { product ->
            val offerDetails = product.subscriptionOfferDetails?.firstOrNull()
            val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
            
            when (product.productId) {
                SubscriptionPlans.WEEKLY_PRODUCT_ID -> {
                    SubscriptionPlan(
                        id = product.productId,
                        title = "Weekly",
                        price = pricingPhase?.formattedPrice ?: "$4.99",
                        period = "Week",
                        discount = null,
                        isPopular = false
                    )
                }
                SubscriptionPlans.MONTHLY_PRODUCT_ID -> {
                    SubscriptionPlan(
                        id = product.productId,
                        title = "Monthly",
                        price = pricingPhase?.formattedPrice ?: "$9.99",
                        period = "Month",
                        discount = "Save 50%",
                        isPopular = true
                    )
                }
                else -> null
            }
        }
    }

    /**
     * Select a subscription plan
     */
    fun selectPlan(plan: SubscriptionPlan) {
        _selectedPlan.value = plan
    }

    /**
     * Purchase selected plan
     */
    fun purchaseSelectedPlan(activity: Activity) {
        val plan = _selectedPlan.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // Find the product details for the selected plan
            val productDetails = _availableProducts.value.find { it.productId == plan.id }
            
            if (productDetails != null) {
                val result = billingRepository.launchPurchaseFlow(activity, productDetails)
                
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Purchase flow launched successfully
                    println("✅ Purchase flow launched")
                } else {
                    _isLoading.value = false
                    _uiState.value = PremiumUiState.Error("Failed to start purchase: ${result.debugMessage}")
                }
            } else {
                _isLoading.value = false
                _uiState.value = PremiumUiState.Error("Product not available")
            }
        }
    }

    /**
     * Check if user is premium
     */
    fun checkPremiumStatus(onPremium: () -> Unit) {
        viewModelScope.launch {
            val isPremium = premiumPrefs.getIsPremiumSync()
            if (isPremium) {
                onPremium()
            }
        }
    }

    /**
     * Reset error state
     */
    fun resetError() {
        if (_uiState.value is PremiumUiState.Error) {
            _uiState.value = PremiumUiState.Success(SubscriptionPlans.getAll())
        }
        billingRepository.resetBillingState()
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.resetBillingState()
    }
}

/**
 * Premium UI State
 */
sealed class PremiumUiState {
    object Loading : PremiumUiState()
    data class Success(val plans: List<SubscriptionPlan>) : PremiumUiState()
    data class Error(val message: String) : PremiumUiState()
}

