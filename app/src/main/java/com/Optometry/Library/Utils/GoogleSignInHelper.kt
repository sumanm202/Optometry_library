package com.Optometry.Library.Utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.Optometry.Library.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    fun getSignInIntent(): Intent {
        // Sign out first to force account selection
        googleSignInClient.signOut()
        return googleSignInClient.signInIntent
    }
    
    suspend fun handleSignInResult(data: Intent?): GoogleSignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                
                GoogleSignInResult.Success(authResult.user)
            } else {
                GoogleSignInResult.Error("Google Sign-In account is null")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignInHelper", "Google sign in failed: ${e.message}")
            GoogleSignInResult.Error("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Firebase auth failed: ${e.message}")
            GoogleSignInResult.Error("Authentication failed: ${e.message}")
        }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    fun isUserSignedIn(): Boolean {
        return getCurrentUser() != null
    }
}

sealed class GoogleSignInResult {
    data class Success(val user: FirebaseUser?) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Loading : GoogleSignInResult()
    object Idle : GoogleSignInResult()
}