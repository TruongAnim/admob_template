package com.truonganim.admob.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.truonganim.admob.datastore.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Premium Preferences Manager
 * Singleton class to manage premium/subscription status using DataStore
 * Follows the same pattern as PreferencesManager
 */
class PremiumPreferencesManager private constructor(private val context: Context) {

    // StateFlow for real-time premium status updates
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    companion object {
        private const val DATASTORE_NAME = "premium_preferences"

        @Volatile
        private var instance: PremiumPreferencesManager? = null

        fun getInstance(context: Context): PremiumPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PremiumPreferencesManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }

        // Extension property for DataStore
        private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
    }

    /**
     * Load premium status from DataStore
     * Should be called on app start
     */
    suspend fun loadPremiumStatus() {
        val isPremium = getIsPremiumSync()
        _isPremium.value = isPremium
        println("💎 Premium status loaded: $isPremium")
    }

    /**
     * Get premium status as Flow
     */
    fun getIsPremium(): Flow<Boolean> {
        return context.premiumDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.IS_PREMIUM] ?: false
            }
    }

    /**
     * Get premium status synchronously
     */
    suspend fun getIsPremiumSync(): Boolean {
        return getIsPremium().first()
    }

    /**
     * Set premium status
     */
    suspend fun setIsPremium(isPremium: Boolean) {
        context.premiumDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = isPremium
        }
        _isPremium.value = isPremium
        println("💎 Premium status updated: $isPremium")
    }

    /**
     * Save purchase details
     */
    suspend fun savePurchaseDetails(
        productId: String,
        purchaseToken: String,
        purchaseTime: Long
    ) {
        context.premiumDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = true
            preferences[PreferencesKeys.PREMIUM_PRODUCT_ID] = productId
            preferences[PreferencesKeys.PREMIUM_PURCHASE_TOKEN] = purchaseToken
            preferences[PreferencesKeys.PREMIUM_PURCHASE_TIME] = purchaseTime
        }
        _isPremium.value = true
        println("💎 Purchase details saved: productId=$productId, time=$purchaseTime")
    }

    /**
     * Get purchase token
     */
    suspend fun getPurchaseToken(): String? {
        return context.premiumDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.PREMIUM_PURCHASE_TOKEN]
            }
            .first()
    }

    /**
     * Get product ID
     */
    suspend fun getProductId(): String? {
        return context.premiumDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.PREMIUM_PRODUCT_ID]
            }
            .first()
    }

    /**
     * Clear all premium data (for testing or when subscription expires)
     */
    suspend fun clearPremiumData() {
        context.premiumDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.IS_PREMIUM)
            preferences.remove(PreferencesKeys.PREMIUM_PRODUCT_ID)
            preferences.remove(PreferencesKeys.PREMIUM_PURCHASE_TOKEN)
            preferences.remove(PreferencesKeys.PREMIUM_PURCHASE_TIME)
        }
        _isPremium.value = false
        println("💎 Premium data cleared")
    }
}

