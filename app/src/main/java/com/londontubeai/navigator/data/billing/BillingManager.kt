package com.londontubeai.navigator.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class BillingState(
    val isPremium: Boolean = false,
    val isConnected: Boolean = false,
    val monthlyPrice: String? = null,
    val annualPrice: String? = null,
    val lifetimePrice: String? = null,
    val purchaseInProgress: Boolean = false,
    val error: String? = null,
)

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_ANNUAL = "premium_annual"
        const val PRODUCT_LIFETIME = "premium_lifetime"
    }

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    private var productDetailsList = mutableMapOf<String, com.android.billingclient.api.ProductDetails>()

    fun startConnection() {
        Log.d(TAG, "Starting billing connection...")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected successfully")
                    _billingState.value = _billingState.value.copy(isConnected = true, error = null)
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                    _billingState.value = _billingState.value.copy(
                        isConnected = false,
                        error = "Billing unavailable: ${result.debugMessage}",
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                _billingState.value = _billingState.value.copy(isConnected = false)
            }
        })
    }

    private fun queryProducts() {
        // Query subscriptions (monthly + annual)
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ANNUAL)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(subParams) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                detailsList.forEach { details ->
                    productDetailsList[details.productId] = details
                    val price = details.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    when (details.productId) {
                        PRODUCT_MONTHLY -> _billingState.value = _billingState.value.copy(monthlyPrice = price)
                        PRODUCT_ANNUAL -> _billingState.value = _billingState.value.copy(annualPrice = price)
                    }
                }
                Log.d(TAG, "Loaded ${detailsList.size} subscription products")
            } else {
                Log.e(TAG, "Failed to query subs: ${result.debugMessage}")
            }
        }

        // Query in-app (lifetime)
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                detailsList.forEach { details ->
                    productDetailsList[details.productId] = details
                    if (details.productId == PRODUCT_LIFETIME) {
                        _billingState.value = _billingState.value.copy(
                            lifetimePrice = details.oneTimePurchaseOfferDetails?.formattedPrice,
                        )
                    }
                }
                Log.d(TAG, "Loaded ${detailsList.size} in-app products")
            } else {
                Log.e(TAG, "Failed to query in-app: ${result.debugMessage}")
            }
        }
    }

    private fun queryExistingPurchases() {
        // Check subscriptions
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasActiveSub) {
                    _billingState.value = _billingState.value.copy(isPremium = true)
                    Log.d(TAG, "Active subscription found")
                }
            }
        }

        // Check in-app purchases (lifetime)
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasLifetime = purchases.any {
                    it.products.contains(PRODUCT_LIFETIME) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasLifetime) {
                    _billingState.value = _billingState.value.copy(isPremium = true)
                    Log.d(TAG, "Lifetime purchase found")
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val details = productDetailsList[productId]
        if (details == null) {
            Log.e(TAG, "Product details not found for $productId")
            _billingState.value = _billingState.value.copy(error = "Product not available. Please try again.")
            return
        }

        _billingState.value = _billingState.value.copy(purchaseInProgress = true, error = null)

        val flowParams = if (productId == PRODUCT_LIFETIME) {
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                )
                .build()
        } else {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
                .build()
        }

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Launch billing flow failed: ${result.debugMessage}")
            _billingState.value = _billingState.value.copy(
                purchaseInProgress = false,
                error = "Could not start purchase: ${result.debugMessage}",
            )
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        _billingState.value = _billingState.value.copy(purchaseInProgress = false)

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
                        _billingState.value = _billingState.value.copy(isPremium = true, error = null)
                        Log.d(TAG, "Purchase successful: ${purchase.products}")
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
                _billingState.value = _billingState.value.copy(error = null)
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${result.debugMessage}")
                _billingState.value = _billingState.value.copy(
                    error = "Purchase failed. Please try again.",
                )
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                } else {
                    Log.e(TAG, "Acknowledge failed: ${result.debugMessage}")
                }
            }
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            _billingState.value = _billingState.value.copy(error = "Billing service not connected. Please try again.")
            startConnection()
            return
        }
        queryExistingPurchases()
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    fun clearError() {
        _billingState.value = _billingState.value.copy(error = null)
    }
}
