package com.truonganim.admob.billing

/**
 * Subscription Plan data class
 * Represents a subscription plan with its details
 */
data class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    val period: String,
    val discount: String? = null,
    val isPopular: Boolean = false
)

/**
 * Predefined subscription plans
 */
object SubscriptionPlans {
    const val WEEKLY_PRODUCT_ID = "weekly_premium"
    const val MONTHLY_PRODUCT_ID = "monthly_premium"
    
    // For testing purposes - these will be replaced with actual prices from Google Play
    val WEEKLY = SubscriptionPlan(
        id = WEEKLY_PRODUCT_ID,
        title = "Weekly",
        price = "$4.99",
        period = "Week",
        discount = null,
        isPopular = false
    )
    
    val MONTHLY = SubscriptionPlan(
        id = MONTHLY_PRODUCT_ID,
        title = "Monthly",
        price = "$9.99",
        period = "Month",
        discount = "Save 50%",
        isPopular = true
    )
    
    fun getAll() = listOf(WEEKLY, MONTHLY)
}

