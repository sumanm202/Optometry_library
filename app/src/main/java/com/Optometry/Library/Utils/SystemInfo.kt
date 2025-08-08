package com.Optometry.Library.Utils

import android.content.Context
import android.os.Build
import android.view.WindowManager
import android.view.Display
import android.util.DisplayMetrics

/**
 * System Information Utility
 * Provides device-specific information for universal compatibility
 */
object SystemInfo {
    
    /**
     * Get device manufacturer
     */
    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }
    
    /**
     * Get device model
     */
    fun getModel(): String {
        return Build.MODEL
    }
    
    /**
     * Get Android version
     */
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
    
    /**
     * Get Android SDK level
     */
    fun getAndroidSDK(): Int {
        return Build.VERSION.SDK_INT
    }
    
    /**
     * Check if device has physical back button
     */
    fun hasPhysicalBackButton(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }
    
    /**
     * Check if device supports gesture navigation
     */
    fun supportsGestureNavigation(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
    
    /**
     * Get screen dimensions
     */
    fun getScreenDimensions(context: Context): Pair<Int, Int> {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            Pair(metrics.widthPixels, metrics.heightPixels)
        } catch (e: Exception) {
            Pair(1080, 1920) // Default fallback
        }
    }
    
    /**
     * Get screen density
     */
    fun getScreenDensity(context: Context): Float {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            metrics.density
        } catch (e: Exception) {
            2.0f // Default fallback
        }
    }
    
    /**
     * Check if device has notch
     */
    fun hasNotch(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val cutout = display.cutout
                cutout != null
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get device type (phone, tablet, foldable)
     */
    fun getDeviceType(context: Context): DeviceType {
        return try {
            val screenDimensions = getScreenDimensions(context)
            val width = screenDimensions.first
            val height = screenDimensions.second
            val density = getScreenDensity(context)
            
            val widthInches = width / (density * 160)
            val heightInches = height / (density * 160)
            val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
            
            when {
                diagonalInches >= 7.0 -> DeviceType.TABLET
                diagonalInches >= 6.0 -> DeviceType.LARGE_PHONE
                else -> DeviceType.PHONE
            }
        } catch (e: Exception) {
            DeviceType.PHONE
        }
    }
    
    /**
     * Check if device supports edge-to-edge display
     */
    fun supportsEdgeToEdge(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
    
    /**
     * Get system UI flags for edge-to-edge support
     */
    fun getEdgeToEdgeFlags(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.view.WindowInsets.Type.statusBars() or
                android.view.WindowInsets.Type.navigationBars() or
                android.view.WindowInsets.Type.systemBars()
            } else {
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } catch (e: Exception) {
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
    
    /**
     * Get device capabilities summary
     */
    fun getDeviceCapabilities(context: Context): DeviceCapabilities {
        return try {
            DeviceCapabilities(
                manufacturer = getManufacturer(),
                model = getModel(),
                androidVersion = getAndroidVersion(),
                androidSDK = getAndroidSDK(),
                hasPhysicalBackButton = hasPhysicalBackButton(),
                supportsGestureNavigation = supportsGestureNavigation(),
                hasNotch = hasNotch(context),
                deviceType = getDeviceType(context),
                supportsEdgeToEdge = supportsEdgeToEdge(context),
                screenDimensions = getScreenDimensions(context),
                screenDensity = getScreenDensity(context)
            )
        } catch (e: Exception) {
            // Return default capabilities if anything fails
            DeviceCapabilities(
                manufacturer = "Unknown",
                model = "Unknown",
                androidVersion = "Unknown",
                androidSDK = Build.VERSION.SDK_INT,
                hasPhysicalBackButton = false,
                supportsGestureNavigation = true,
                hasNotch = false,
                deviceType = DeviceType.PHONE,
                supportsEdgeToEdge = false,
                screenDimensions = Pair(1080, 1920),
                screenDensity = 2.0f
            )
        }
    }
}

/**
 * Device type enum
 */
enum class DeviceType {
    PHONE,
    LARGE_PHONE,
    TABLET,
    FOLDABLE
}

/**
 * Device capabilities data class
 */
data class DeviceCapabilities(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val androidSDK: Int,
    val hasPhysicalBackButton: Boolean,
    val supportsGestureNavigation: Boolean,
    val hasNotch: Boolean,
    val deviceType: DeviceType,
    val supportsEdgeToEdge: Boolean,
    val screenDimensions: Pair<Int, Int>,
    val screenDensity: Float
)
