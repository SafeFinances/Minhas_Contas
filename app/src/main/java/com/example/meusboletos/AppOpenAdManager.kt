package com.example.meusboletos

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val myApplication: MyApplication) : DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    // ID de TESTE do Google para Anúncio de Abertura. Troque pelo seu ID real antes de publicar.
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    init {
        // Observa o ciclo de vida do app
        myApplication.lifecycle.addObserver(this)
    }

    // Chamado quando o app vem para o primeiro plano (ex: ao abrir ou voltar para ele)
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAdIfAvailable()
    }

    private fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    Log.d("AppOpenAdManager", "Anúncio de abertura carregado.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.d("AppOpenAdManager", "Falha ao carregar anúncio: ${loadAdError.message}")
                }
            }
        )
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            val fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d("AppOpenAdManager", "Anúncio dispensado.")
                    loadAd(myApplication) // Carrega o próximo
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d("AppOpenAdManager", "Falha ao mostrar anúncio: ${adError.message}")
                    loadAd(myApplication) // Carrega o próximo
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    Log.d("AppOpenAdManager", "Anúncio exibido.")
                }
            }

            // Pede para o anúncio se registrar com o "gerenciador de tela cheia"
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            // Pede para o anúncio ser mostrado na Activity atual
            myApplication.currentActivity?.let { appOpenAd?.show(it) }

        } else {
            Log.d("AppOpenAdManager", "Anúncio não está pronto ainda, carregando...")
            loadAd(myApplication)
        }
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }
}