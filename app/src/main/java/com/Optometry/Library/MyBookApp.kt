package com.Optometry.Library

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.ads.MobileAds

class MyBookApp : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        // Enable Firebase Database offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of available disk space
                    .build()
            }
            .respectCacheHeaders(false) // Always cache images regardless of cache headers
            .crossfade(true) // Enable crossfade animation
            .crossfade(300) // 300ms crossfade duration
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}