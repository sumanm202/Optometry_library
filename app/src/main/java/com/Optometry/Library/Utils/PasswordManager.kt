package com.Optometry.Library.Utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PasswordManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular SharedPreferences if EncryptedSharedPreferences fails
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    }
    
    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit()
            .putString("saved_email", email)
            .putString("saved_password", password)
            .putBoolean("remember_password", true)
            .apply()
    }
    
    fun getSavedCredentials(): Pair<String?, String?> {
        val email = sharedPreferences.getString("saved_email", null)
        val password = sharedPreferences.getString("saved_password", null)
        return Pair(email, password)
    }
    
    fun isRememberPasswordEnabled(): Boolean {
        return sharedPreferences.getBoolean("remember_password", false)
    }
    
    fun clearSavedCredentials() {
        sharedPreferences.edit()
            .remove("saved_email")
            .remove("saved_password")
            .putBoolean("remember_password", false)
            .apply()
    }
    
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit()
            .putBoolean("is_logged_in", isLoggedIn)
            .apply()
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    fun clearLoginState() {
        sharedPreferences.edit()
            .putBoolean("is_logged_in", false)
            .apply()
    }
}