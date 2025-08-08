package com.Optometry.Library.Utils

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PhoneAuthHelper(private val activity: Activity) {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    fun sendOTP(
        phoneNumber: String,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (credential: PhoneAuthCredential, smsCode: String?) -> Unit,
        onVerificationFailed: (exception: FirebaseException) -> Unit
    ) {
        Log.d("PhoneAuth", "🚀 Starting OTP send for: $phoneNumber")
        
        // Validate phone number format
        if (!phoneNumber.startsWith("+")) {
            Log.e("PhoneAuth", "❌ Invalid phone format: $phoneNumber (must start with +)")
            onVerificationFailed(FirebaseException("Invalid phone number format. Must start with country code (e.g., +91XXXXXXXXXX)"))
            return
        }
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("PhoneAuth", "✅ Verification completed automatically")
                onVerificationCompleted(credential, credential.smsCode)
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "❌ Verification failed: ${e.message}")
                Log.e("PhoneAuth", "❌ Error details: ${e.localizedMessage}")
                onVerificationFailed(e)
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("PhoneAuth", "📱 Code sent successfully to $phoneNumber")
                Log.d("PhoneAuth", "📱 Verification ID: $verificationId")
                this@PhoneAuthHelper.verificationId = verificationId
                this@PhoneAuthHelper.resendToken = token
                onCodeSent(verificationId, token)
            }
            
            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                Log.w("PhoneAuth", "⏰ Auto-retrieval timeout for $phoneNumber")
            }
        }
        
        try {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
                
            Log.d("PhoneAuth", "📞 Calling PhoneAuthProvider.verifyPhoneNumber")
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("PhoneAuth", "❌ Exception during OTP send: ${e.message}")
            onVerificationFailed(FirebaseException("Failed to send OTP: ${e.message}"))
        }
    }
    
    suspend fun verifyOTP(otp: String): PhoneAuthResult {
        return try {
            val verificationId = this.verificationId 
                ?: return PhoneAuthResult.Error("No verification ID found")
            
            Log.d("PhoneAuth", "🔐 Verifying OTP: $otp")
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            
            Log.d("PhoneAuth", "✅ OTP verification successful")
            PhoneAuthResult.Success(authResult.user)
            
        } catch (e: Exception) {
            Log.e("PhoneAuth", "❌ OTP verification failed: ${e.message}")
            PhoneAuthResult.Error("Invalid OTP: ${e.message}")
        }
    }
    
    fun resendOTP(
        phoneNumber: String,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationFailed: (exception: FirebaseException) -> Unit
    ) {
        Log.d("PhoneAuth", "🔄 Resending OTP to: $phoneNumber")
        
        if (resendToken == null) {
            Log.e("PhoneAuth", "❌ No resend token available")
            onVerificationFailed(FirebaseException("Cannot resend OTP. Please try again."))
            return
        }
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
            
            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "❌ Resend verification failed: ${e.message}")
                onVerificationFailed(e)
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("PhoneAuth", "📱 Code resent successfully to $phoneNumber")
                this@PhoneAuthHelper.verificationId = verificationId
                this@PhoneAuthHelper.resendToken = token
                onCodeSent(verificationId, token)
            }
        }
        
        try {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken!!)
                .build()
                
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("PhoneAuth", "❌ Exception during OTP resend: ${e.message}")
            onVerificationFailed(FirebaseException("Failed to resend OTP: ${e.message}"))
        }
    }
}

sealed class PhoneAuthResult {
    data class Success(val user: FirebaseUser?) : PhoneAuthResult()
    data class Error(val message: String) : PhoneAuthResult()
    object Loading : PhoneAuthResult()
    object Idle : PhoneAuthResult()
}