package com.Optometry.Library

import android.app.Application
import com.Optometry.Library.Utils.loadAdUnits
import com.Optometry.Library.Utils.loadInterstitialAdIfNull
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase

class MyBookApp() : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {
            loadAdUnits {
                loadInterstitialAdIfNull(this)
            }
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}