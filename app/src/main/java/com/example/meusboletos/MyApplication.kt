package com.example.meusboletos

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa a biblioteca de data quando o app é criado
        AndroidThreeTen.init(this)
    }
}