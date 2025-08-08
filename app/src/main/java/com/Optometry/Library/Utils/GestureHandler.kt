package com.Optometry.Library.Utils

import android.content.Context
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Universal Gesture Handler for different phone types
 * Handles swipe gestures, edge gestures, and various phone-specific controls
 */
class GestureHandler {
    companion object {
        /**
         * Detect swipe gestures for navigation
         */
        fun Modifier.swipeGesture(
            onSwipeLeft: () -> Unit = {},
            onSwipeRight: () -> Unit = {},
            onSwipeUp: () -> Unit = {},
            onSwipeDown: () -> Unit = {},
            threshold: Float = 100f
        ): Modifier {
            return this.pointerInput(Unit) {
                var startX = 0f
                var startY = 0f
                
                detectDragGestures(
                    onDragStart = { offset ->
                        startX = offset.x
                        startY = offset.y
                    },
                    onDragEnd = {
                        // Gesture ended
                    },
                    onDragCancel = {
                        // Gesture cancelled
                    }
                ) { change, _ ->
                    val endX = change.position.x
                    val endY = change.position.y
                    
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    
                    // Determine swipe direction
                    when {
                        abs(deltaX.toDouble()) > abs(deltaY.toDouble()) && abs(deltaX.toDouble()) > threshold.toDouble() -> {
                            if (deltaX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                        abs(deltaY.toDouble()) > abs(deltaX.toDouble()) && abs(deltaY.toDouble()) > threshold.toDouble() -> {
                            if (deltaY > 0) {
                                onSwipeDown()
                            } else {
                                onSwipeUp()
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * Detect edge gestures (for phones with edge-to-edge gestures)
         */
        fun Modifier.edgeGesture(
            onEdgeSwipe: () -> Unit = {},
            edgeThreshold: Float = 50f
        ): Modifier {
            return this.pointerInput(Unit) {
                var startX = 0f
                
                detectDragGestures(
                    onDragStart = { offset ->
                        startX = offset.x
                    },
                    onDragEnd = {
                        // Gesture ended
                    },
                    onDragCancel = {
                        // Gesture cancelled
                    }
                ) { change, _ ->
                    val currentX = change.position.x
                    val deltaX = currentX - startX
                    
                    // Check if gesture started from edge
                    if (startX <= edgeThreshold || startX >= size.width - edgeThreshold) {
                        if (abs(deltaX.toDouble()) > 100.0) {
                            onEdgeSwipe()
                        }
                    }
                }
            }
        }
        
        /**
         * Detect long press gestures
         */
        fun Modifier.longPressGesture(
            onLongPress: () -> Unit = {}
        ): Modifier {
            return this.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
        }
        
        /**
         * Detect double tap gestures
         */
        fun Modifier.doubleTapGesture(
            onDoubleTap: () -> Unit = {}
        ): Modifier {
            return this.pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() }
                )
            }
        }
    }
}

/**
 * Phone-specific gesture configurations
 */
object PhoneGestureConfig {
    /**
     * Configuration for different phone types
     */
    enum class PhoneType {
        SAMSUNG,    // Edge gestures, One UI
        XIAOMI,     // MIUI gestures
        OPPO,       // ColorOS gestures
        HUAWEI,     // EMUI gestures
        GOOGLE,     // Stock Android gestures
        ONEPLUS,    // OxygenOS gestures
        OTHER       // Generic
    }
    
    /**
     * Get phone type based on manufacturer
     */
    fun getPhoneType(context: Context): PhoneType {
        return try {
            val manufacturer = android.os.Build.MANUFACTURER.lowercase()
            when {
                manufacturer.contains("samsung") -> PhoneType.SAMSUNG
                manufacturer.contains("xiaomi") -> PhoneType.XIAOMI
                manufacturer.contains("oppo") -> PhoneType.OPPO
                manufacturer.contains("huawei") -> PhoneType.HUAWEI
                manufacturer.contains("google") -> PhoneType.GOOGLE
                manufacturer.contains("oneplus") -> PhoneType.ONEPLUS
                else -> PhoneType.OTHER
            }
        } catch (e: Exception) {
            PhoneType.OTHER
        }
    }
    
    /**
     * Get gesture configuration for specific phone type
     */
    fun getGestureConfig(phoneType: PhoneType): GestureConfig {
        return when (phoneType) {
            PhoneType.SAMSUNG -> GestureConfig(
                edgeThreshold = 30f,
                swipeThreshold = 80f,
                supportsEdgeGestures = true
            )
            PhoneType.XIAOMI -> GestureConfig(
                edgeThreshold = 25f,
                swipeThreshold = 70f,
                supportsEdgeGestures = true
            )
            PhoneType.OPPO -> GestureConfig(
                edgeThreshold = 35f,
                swipeThreshold = 90f,
                supportsEdgeGestures = true
            )
            PhoneType.HUAWEI -> GestureConfig(
                edgeThreshold = 40f,
                swipeThreshold = 85f,
                supportsEdgeGestures = true
            )
            PhoneType.GOOGLE -> GestureConfig(
                edgeThreshold = 50f,
                swipeThreshold = 100f,
                supportsEdgeGestures = true
            )
            PhoneType.ONEPLUS -> GestureConfig(
                edgeThreshold = 30f,
                swipeThreshold = 75f,
                supportsEdgeGestures = true
            )
            PhoneType.OTHER -> GestureConfig(
                edgeThreshold = 50f,
                swipeThreshold = 100f,
                supportsEdgeGestures = false
            )
        }
    }
}

/**
 * Gesture configuration data class
 */
data class GestureConfig(
    val edgeThreshold: Float,
    val swipeThreshold: Float,
    val supportsEdgeGestures: Boolean
)
