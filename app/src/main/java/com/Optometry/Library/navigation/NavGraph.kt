package com.Optometry.Library.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.Optometry.Library.ui.screens.*

@Composable
fun OptometryNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    showExitDialog: Boolean = false,
    onExitDialogDismiss: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        composable(Screen.LoginSignup.route) {
            LoginSignupScreen(navController = navController)
        }
        
        composable(Screen.PasswordReset.route) {
            PasswordResetScreen(navController = navController)
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                showExitDialog = showExitDialog,
                onExitDialogDismiss = onExitDialogDismiss
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        
        composable(Screen.Category.route) {
            CategoryScreen(navController = navController)
        }
        
        composable(
            route = "${Screen.Category.route}/{categoryId}/{categoryTitle}",
            arguments = listOf(
                navArgument("categoryId") { 
                    type = NavType.StringType
                },
                navArgument("categoryTitle") { 
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val categoryTitle = backStackEntry.arguments?.getString("categoryTitle")
            CategoryDetailsScreen(
                navController = navController,
                categoryId = categoryId,
                categoryTitle = categoryTitle
            )
        }
        
        composable(
            route = "${Screen.Details.route}/{bookId}",
            arguments = listOf(navArgument("bookId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            DetailsScreen(
                navController = navController,
                bookId = bookId
            )
        }
        
        composable(Screen.Details.route) {
            DetailsScreen(navController = navController)
        }
        
        composable(
            route = "${Screen.PdfViewer.route}/{bookTitle}",
            arguments = listOf(navArgument("bookTitle") { 
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val bookTitle = backStackEntry.arguments?.getString("bookTitle")
            PdfViewerScreen(
                navController = navController,
                bookTitle = bookTitle
            )
        }
        
        composable(
            route = "${Screen.PdfViewer.route}/{bookTitle}/{pageNumber}",
            arguments = listOf(
                navArgument("bookTitle") { 
                    type = NavType.StringType
                },
                navArgument("pageNumber") { 
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val bookTitle = backStackEntry.arguments?.getString("bookTitle")
            val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: 0
            PdfViewerScreen(
                navController = navController,
                bookTitle = bookTitle,
                pageNumber = pageNumber
            )
        }
        
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(navController = navController)
        }
        
        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }
        
        composable(Screen.PdfTools.route) {
            PdfToolsScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
} 