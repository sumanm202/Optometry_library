package com.Optometry.Library.ViewModels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Optometry.Library.Utils.GoogleSignInHelper
import com.Optometry.Library.Utils.GoogleSignInResult
import com.Optometry.Library.Utils.PasswordManager
import com.Optometry.Library.Utils.PhoneAuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(context: Context) : ViewModel() {
    
    private val googleSignInHelper = GoogleSignInHelper(context)
    private val passwordManager = PasswordManager(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<GoogleSignInResult>(GoogleSignInResult.Idle)
    val authState: StateFlow<GoogleSignInResult> = _authState.asStateFlow()
    
    private val _phoneAuthState = MutableStateFlow<PhoneAuthResult>(PhoneAuthResult.Idle)
    val phoneAuthState: StateFlow<PhoneAuthResult> = _phoneAuthState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Phone auth specific states
    private val _isOTPSent = MutableStateFlow(false)
    val isOTPSent: StateFlow<Boolean> = _isOTPSent.asStateFlow()
    
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    // Persist verificationId between send and verify steps
    private var pendingVerificationId: String? = null
    private var forceResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val _autoRetrievedCode = MutableStateFlow<String?>(null)
    val autoRetrievedCode: StateFlow<String?> = _autoRetrievedCode.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        _currentUser.value = googleSignInHelper.getCurrentUser()
    }
    
    fun getGoogleSignInIntent(): Intent {
        return googleSignInHelper.getSignInIntent()
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = googleSignInHelper.handleSignInResult(data)
            _authState.value = result
            
            when (result) {
                is GoogleSignInResult.Success -> {
                    _currentUser.value = result.user
                    passwordManager.saveLoginState(true)
                    _errorMessage.value = null
                }
                is GoogleSignInResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        googleSignInHelper.signOut()
        passwordManager.clearLoginState()
        passwordManager.clearSavedCredentials()
        _currentUser.value = null
        _authState.value = GoogleSignInResult.Idle
        _phoneAuthState.value = PhoneAuthResult.Idle
        _isOTPSent.value = false
        _errorMessage.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun isUserSignedIn(): Boolean {
        return googleSignInHelper.isUserSignedIn()
    }
    
    // Password management methods
    fun getSavedCredentials(): Pair<String?, String?> {
        return passwordManager.getSavedCredentials()
    }
    
    fun isRememberPasswordEnabled(): Boolean {
        return passwordManager.isRememberPasswordEnabled()
    }
    
    fun isUserLoggedIn(): Boolean {
        return passwordManager.isUserLoggedIn()
    }
    
    // Phone authentication methods
    fun sendOTP(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _phoneNumber.value = phoneNumber
            _errorMessage.value = null
            
            val phoneAuthHelper = com.Optometry.Library.Utils.PhoneAuthHelper(activity)
            
            phoneAuthHelper.sendOTP(
                phoneNumber = phoneNumber,
                onCodeSent = { verificationId, token ->
                    pendingVerificationId = verificationId
                    forceResendToken = token
                    _isOTPSent.value = true
                    _isLoading.value = false
                },
                onVerificationCompleted = { credential, smsCode ->
                    // Auto-verification completed, sign in directly
                    viewModelScope.launch {
                        try {
                            _isLoading.value = true
                            _autoRetrievedCode.value = smsCode
                            val authResult = firebaseAuth.signInWithCredential(credential).await()
                            _currentUser.value = authResult.user
                            _phoneAuthState.value = PhoneAuthResult.Success(authResult.user)
                            passwordManager.saveLoginState(true)
                        } catch (e: Exception) {
                            _errorMessage.value = "Auto verification failed: ${e.message}"
                            _phoneAuthState.value = PhoneAuthResult.Error("${e.message}")
                        } finally {
                            _isLoading.value = false
                        }
                    }
                },
                onVerificationFailed = { exception ->
                    val friendly = when (exception) {
                        is com.google.firebase.FirebaseTooManyRequestsException -> "Too many requests. Try again later."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid phone number format. Use country code, e.g., +91XXXXXXXXXX."
                        else -> "Phone verification failed: ${exception.message ?: "Unknown error"}. Ensure: test phone in Firebase Auth, SHA-1/SHA-256 added to Firebase, and Play Integrity API enabled on the project."
                    }
                    _errorMessage.value = friendly
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun verifyOTP(otp: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val verificationId = pendingVerificationId
                if (verificationId.isNullOrEmpty()) {
                    _errorMessage.value = "No verification ID. Please resend OTP."
                    _phoneAuthState.value = PhoneAuthResult.Error("No verification ID")
                    return@launch
                }
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                _currentUser.value = authResult.user
                _phoneAuthState.value = PhoneAuthResult.Success(authResult.user)
                passwordManager.saveLoginState(true)
                _isOTPSent.value = false
                pendingVerificationId = null
                _autoRetrievedCode.value = null
            } catch (e: Exception) {
                _errorMessage.value = "OTP verification failed: ${e.message}"
                _phoneAuthState.value = PhoneAuthResult.Error("${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendOTP(activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val phoneAuthHelper = com.Optometry.Library.Utils.PhoneAuthHelper(activity)
            phoneAuthHelper.resendOTP(
                phoneNumber = _phoneNumber.value,
                onCodeSent = { verificationId, token ->
                    pendingVerificationId = verificationId
                    forceResendToken = token
                    _isOTPSent.value = true
                    _isLoading.value = false
                },
                onVerificationFailed = { exception ->
                    _errorMessage.value = "Resend failed: ${exception.message}"
                    _isLoading.value = false
                }
            )
        }
    }
    
    // Traditional email/password authentication methods
    fun signInWithEmailAndPassword(email: String, password: String, rememberPassword: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = authResult.user
                _authState.value = GoogleSignInResult.Success(authResult.user)
                passwordManager.saveLoginState(true)
                if (rememberPassword) {
                    passwordManager.saveCredentials(email, password)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signUpWithEmailAndPassword(email: String, password: String, rememberPassword: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _currentUser.value = authResult.user
                _authState.value = GoogleSignInResult.Success(authResult.user)
                passwordManager.saveLoginState(true)
                if (rememberPassword) {
                    passwordManager.saveCredentials(email, password)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Signup failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}