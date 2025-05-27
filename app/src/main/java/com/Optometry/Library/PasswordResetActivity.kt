package com.Optometry.Library

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.Optometry.Library.databinding.ActivityPasswordResetBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class PasswordResetActivity : AppCompatActivity() {
    private val binding: ActivityPasswordResetBinding by lazy {
        ActivityPasswordResetBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth=Firebase.auth

        binding.PasswordResetbtn.setOnClickListener { resetPassword() }

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }


    private fun resetPassword(){
        val email=binding.PasswordResetEmail.text.toString()
        if (isValidEmail(email)==true) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password Reset Email Send Successfully", Toast.LENGTH_LONG)
                        .show()
                    startActivity(Intent(this, Activity_login_sign_up::class.java))

                }
            }
        }else{
                Toast.makeText(this,"Please Fill Proper Email", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")
        return pattern.matches(email)

    }



