package com.Optometry.Library.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hbb20.CountryCodePicker
import com.Optometry.Library.Utils.GoogleSignInResult
import com.Optometry.Library.Utils.PhoneAuthResult
import com.Optometry.Library.ViewModels.AuthViewModel
import com.Optometry.Library.ViewModels.AuthViewModelFactory
import com.Optometry.Library.navigation.Screen
import android.app.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(false) }
    
    // Phone auth states
    var showPhoneLogin by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+1") }
    var localPhoneNumber by remember { mutableStateOf("") }
    
    // Observe auth states
    val authState by authViewModel.authState.collectAsState()
    val phoneAuthState by authViewModel.phoneAuthState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val isOTPSent by authViewModel.isOTPSent.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val autoCode by authViewModel.autoRetrievedCode.collectAsState()
    
    // Load saved credentials if available
    LaunchedEffect(Unit) {
        if (authViewModel.isRememberPasswordEnabled()) {
            val (savedEmail, savedPassword) = authViewModel.getSavedCredentials()
            savedEmail?.let { email = it }
            savedPassword?.let { password = it }
            rememberPassword = true
        }
    }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authViewModel.handleGoogleSignInResult(result.data)
    }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (val currentAuthState = authState) {
            is GoogleSignInResult.Success -> {
                val welcomeMessage = if (currentAuthState.user?.displayName != null) {
                    "Welcome, ${currentAuthState.user.displayName}!"
                } else {
                    "Welcome! You are now signed in."
                }
                Toast.makeText(context, welcomeMessage, Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.LoginSignup.route) { inclusive = true }
                }
            }
            is GoogleSignInResult.Error -> {
                Toast.makeText(context, currentAuthState.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    
    // Handle phone auth state changes
    LaunchedEffect(phoneAuthState) {
        when (val currentPhoneAuthState = phoneAuthState) {
            is PhoneAuthResult.Success -> {
                Toast.makeText(context, "Phone verification successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.LoginSignup.route) { inclusive = true }
                }
            }
            is PhoneAuthResult.Error -> {
                Toast.makeText(context, currentPhoneAuthState.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    
    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(800),
                    initialOffsetY = { -it }
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = com.Optometry.Library.R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(112.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isLogin) "Welcome Back" else "Create Account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = if (isLogin) "Sign in to continue" else "Join our community",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Form
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(800, delayMillis = 200),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isLogin) "Sign in to your account" else "Create your account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, "Email")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, "Password")
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        // Remember Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberPassword,
                                onCheckedChange = { rememberPassword = it }
                            )
                            Text(
                                text = "Remember Password",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Forgot Password below (Login only)
                        if (isLogin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { navController.navigate(Screen.PasswordReset.route) }
                                ) {
                                    Text("Forgot Password?", fontSize = 14.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Submit Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // Handle login/signup with email and password
                                if (isLogin) {
                                    authViewModel.signInWithEmailAndPassword(email, password, rememberPassword)
                                } else {
                                    authViewModel.signUpWithEmailAndPassword(email, password, rememberPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = if (isLogin) "Sign In" else "Create Account",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // OR divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = "OR",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = {
                                val signInIntent = authViewModel.getGoogleSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val googleRes = com.Optometry.Library.R.drawable.google
                                if (googleRes != 0) {
                                    Image(
                                        painter = painterResource(id = googleRes),
                                        contentDescription = "Google",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(text = if (isLoading) "Signing in..." else "Continue with Google")
                            }
                        }

                        // Phone Sign-In Button
                        OutlinedButton(
                            onClick = { showPhoneLogin = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Continue with Phone")
                            }
                        }
                        
                        // Toggle Login/Signup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isLogin) "Don't have an account?" else "Already have an account?",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                softWrap = false
                            )
                            TextButton(
                                onClick = { isLogin = !isLogin }
                            ) {
                                Text(
                                    text = if (isLogin) "Sign Up" else "Sign In",
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // If auto-retrieved code appears while dialog is open, auto-fill OTP
    LaunchedEffect(autoCode) {
        if (autoCode != null) {
            otp = autoCode ?: ""
        }
    }

    // Phone Login Dialog
    if (showPhoneLogin) {
        AlertDialog(
            onDismissRequest = { 
                showPhoneLogin = false
                otp = ""
                phoneNumber = ""
                localPhoneNumber = ""
            },
            title = { 
                Text(if (isOTPSent) "Enter OTP" else "Phone Login") 
            },
            text = {
                Column {
                    if (!isOTPSent) {
                        // Country Code Picker (Android View inside Compose)
                        AndroidView<CountryCodePicker>(
                            factory = { ctx ->
                                CountryCodePicker(ctx).apply {
                                    setAutoDetectedCountry(true)
                                    setOnCountryChangeListener {
                                        selectedCountryCode = "+" + this.selectedCountryCode
                                    }
                                }
                            },
                            update = { view: CountryCodePicker ->
                                // Keep selected code updated on recomposition
                                selectedCountryCode = "+" + view.selectedCountryCode
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = localPhoneNumber,
                            onValueChange = { localPhoneNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Phone Number") },
                            placeholder = { Text("0123456789") },
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Will send OTP to $selectedCountryCode${if (localPhoneNumber.isNotEmpty()) localPhoneNumber else "<number>"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it },
                            label = { Text("OTP Code") },
                            placeholder = { Text("123456") },
                            leadingIcon = { Icon(Icons.Default.Security, "OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter the 6-digit code sent to $phoneNumber",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isOTPSent) {
                            if (localPhoneNumber.isNotEmpty()) {
                                val full = "$selectedCountryCode$localPhoneNumber".trim()
                                // Validate phone number format
                                if (full.length < 10) {
                                    Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                phoneNumber = full
                                authViewModel.sendOTP(full, context as Activity)
                            } else {
                                Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (otp.length >= 6) {
                                authViewModel.verifyOTP(otp, context as Activity)
                                showPhoneLogin = false
                            } else {
                                Toast.makeText(context, "Please enter OTP", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isOTPSent) "Verify OTP" else "Send OTP")
                    }
                }
            },
            dismissButton = {
                Row {
                    if (isOTPSent) {
                        TextButton(onClick = { authViewModel.resendOTP(context as Activity) }) {
                            Text("Resend")
                        }
                    }
                    TextButton(
                        onClick = { 
                            showPhoneLogin = false
                            otp = ""
                            phoneNumber = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
} 