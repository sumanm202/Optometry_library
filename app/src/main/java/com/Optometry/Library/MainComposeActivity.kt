package com.Optometry.Library

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.Optometry.Library.navigation.OptometryNavGraph
import com.Optometry.Library.ui.theme.OptometryLibraryTheme
import com.Optometry.Library.Utils.BackButtonHandler
import com.Optometry.Library.Utils.rememberExitDialogState

class MainComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OptometryLibraryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val showExitDialogState = rememberExitDialogState()
                    
                    OptometryNavGraph(
                        navController = navController,
                        showExitDialog = showExitDialogState.value,
                        onExitDialogDismiss = { BackButtonHandler.dismissExitDialog() }
                    )
                }
            }
        }
    }
    
    /**
     * Handle back button press
     */
    override fun onBackPressed() {
        // This will be handled by the navigation system
        super.onBackPressed()
    }
} 