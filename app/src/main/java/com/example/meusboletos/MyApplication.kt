package com.example.meusboletos

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.jakewharton.threetenabp.AndroidThreeTen

// Nossa classe agora precisa observar as Activities
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        const val CHANNEL_ID = "boleto_vencimento_channel"
    }

    // Variáveis para o guardião do anúncio
    private lateinit var appOpenAdManager: AppOpenAdManager
    var currentActivity: Activity? = null

    val lifecycle: Lifecycle
        get() = ProcessLifecycleOwner.get().lifecycle

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        createNotificationChannel()

        // Inicializa o SDK de anúncios e o nosso guardião
        MobileAds.initialize(this) {}
        registerActivityLifecycleCallbacks(this)
        appOpenAdManager = AppOpenAdManager(this)
    }

    private fun createNotificationChannel() {
        // ... (seu código do canal de notificação continua o mesmo)
    }

    // --- Métodos para observar o ciclo de vida das Activities ---
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}