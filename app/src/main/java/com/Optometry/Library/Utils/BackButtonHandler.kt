package com.Optometry.Library.Utils

import androidx.compose.runtime.*
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal Back Button Handler for different phone types
 * Handles physical back buttons, gesture controls, and navigation
 */
class BackButtonHandler {
    companion object {
        private val _showExitDialog = MutableStateFlow(false)
        val showExitDialog: StateFlow<Boolean> = _showExitDialog
        
        /**
         * Handle back press based on current navigation state
         */
        fun handleBackPress(navController: NavController) {
            when {
                // If we can go back in navigation, do so
                navController.previousBackStackEntry != null -> {
                    navController.popBackStack()
                }
                // If we're at the root (Main screen), show exit dialog
                else -> {
                    _showExitDialog.value = true
                }
            }
        }
        
        /**
         * Dismiss exit dialog
         */
        fun dismissExitDialog() {
            _showExitDialog.value = false
        }
        
        /**
         * Show exit dialog
         */
        fun showExitDialog() {
            _showExitDialog.value = true
        }
        
        /**
         * Exit the app
         */
        fun exitApp() {
            dismissExitDialog()
            // Exit the app
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}

/**
 * Composable function to observe exit dialog state
 */
@Composable
fun rememberExitDialogState(): State<Boolean> {
    return BackButtonHandler.showExitDialog.collectAsState()
}
