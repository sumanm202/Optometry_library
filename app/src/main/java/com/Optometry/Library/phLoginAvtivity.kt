package com.Optometry.Library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.Optometry.Library.databinding.ActivityMainBinding
import com.Optometry.Library.databinding.ActivityPhLoginAvtivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class phLoginAvtivity : AppCompatActivity() {
    private val binding:ActivityPhLoginAvtivityBinding by lazy {
        ActivityPhLoginAvtivityBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.Otpbtn.setOnClickListener {
            val phoneNumber = binding.phNumber.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            }else if (phoneNumber.length<=11){
                Toast.makeText(this, "Please enter correct phone number", Toast.LENGTH_SHORT).show()
            }else {
                sendVerificationCode(phoneNumber)
            }
        }

        binding.verifyOtpbtn.setOnClickListener {
            val otp = binding.otp.text.toString().trim()

            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(otp)
            }
        }
        val controller= WindowInsetsControllerCompat(window ,window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "onVerificationFailed", e)
                    Toast.makeText(this@phLoginAvtivity, "Verification failed", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationId, token)
                    this@phLoginAvtivity.verificationId = verificationId
                    binding.Otpbtn.visibility= View.GONE
                    // Show OTP EditText and Verify Button
                    binding.otp.visibility = View.VISIBLE
                    binding.verifyOtpbtn.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    Toast.makeText(this, "Verification successful", Toast.LENGTH_SHORT).show()
                    // Proceed with your app flow, e.g., move to the next activity
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                }

            }
    }

    companion object {
        private const val TAG = "phLoginAvtivity"
    }

}
