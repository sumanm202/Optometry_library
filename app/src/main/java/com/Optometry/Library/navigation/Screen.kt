package com.Optometry.Library.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object LoginSignup : Screen("login_signup")
    object PasswordReset : Screen("password_reset")
    object Main : Screen("main")
    object Search : Screen("search")
    object Category : Screen("category")
    object Details : Screen("details")
    object PdfViewer : Screen("pdf_viewer")
    object Bookmarks : Screen("bookmarks")
    object Notes : Screen("notes")
    object PdfTools : Screen("pdf_tools")
    object Profile : Screen("profile")
} 