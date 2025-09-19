package com.example.meusboletos

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(private val context: Context, private val onPurchaseSuccess: () -> Unit) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Compra bem-sucedida!
                    Log.d("BillingManager", "Compra bem-sucedida: ${purchase.products.firstOrNull()}")
                    onPurchaseSuccess()
                }
            }
        } else {
            // Lidar com outros erros aqui
            Log.d("BillingManager", "Erro na compra. Código: ${billingResult.responseCode}")
        }
    }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Conexão com a Play Billing estabelecida.")
                    // A conexão foi um sucesso. Podemos verificar compras existentes.
                    queryExistingPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d("BillingManager", "Conexão com a Play Billing perdida.")
                // Tente reconectar se necessário
            }
        })
    }

    // Verifica se o usuário já comprou o item
    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
                // Usuário já tem o passe sem anúncios
                onPurchaseSuccess()
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        // Usaremos o ID de TESTE do Google por enquanto
        val productId = "android.test.purchased"

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                Log.d("BillingManager", "Produto de teste não encontrado.")
            }
        }
    }
}