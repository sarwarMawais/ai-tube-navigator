package com.londontubeai.navigator.ui.screens.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.londontubeai.navigator.data.billing.BillingManager
import com.londontubeai.navigator.data.billing.BillingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
) : ViewModel() {

    val billingState: StateFlow<BillingState> = billingManager.billingState

    init {
        billingManager.startConnection()
    }

    fun purchaseMonthly(activity: Activity) {
        billingManager.launchPurchaseFlow(activity, BillingManager.PRODUCT_MONTHLY)
    }

    fun purchaseAnnual(activity: Activity) {
        billingManager.launchPurchaseFlow(activity, BillingManager.PRODUCT_ANNUAL)
    }

    fun purchaseLifetime(activity: Activity) {
        billingManager.launchPurchaseFlow(activity, BillingManager.PRODUCT_LIFETIME)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun clearError() {
        billingManager.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
