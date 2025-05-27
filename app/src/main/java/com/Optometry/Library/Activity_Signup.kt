package com.Optometry.Library

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.Optometry.Library.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class Activity_Signup : AppCompatActivity() {
    private val binding:ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //Initialize Firebase Auth
        auth= FirebaseAuth.getInstance()


        binding.GoToLogin.setOnClickListener {
            startActivity(Intent(this,Activity_login_sign_up::class.java))
            finish()
        }

        binding.RegisterBtn.setOnClickListener {
            //get text from edit text
            val FirstName=binding.FirstName.text.toString()
            val LastName=binding.LastName.text.toString()
            val Email=binding.Email.text.toString()
            val Password=binding.Password.text.toString()
            val ConfirmPassword=binding.ConfirmPassword.text.toString()

            // check if any fields is blank

            if (FirstName.isEmpty()||LastName.isEmpty()|| Email.isEmpty()||Password.isEmpty()||ConfirmPassword.isEmpty()){
                Toast.makeText(this,"Fill All The Details",Toast.LENGTH_SHORT).show()
            }else if (isValidCredentials(Email,Password)!=true){
                Toast.makeText(this,"Enter Valid Email or Password",Toast.LENGTH_LONG).show()
            }
            else if(Password != ConfirmPassword){
                Toast.makeText(this,"Conform Password Must be Same",Toast.LENGTH_SHORT).show()
            }else{
                auth.createUserWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(this){task ->
                        if(task.isSuccessful){
                            Toast.makeText(this,"Registration Successful",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,Activity_login_sign_up::class.java))
                            finish()
                        }else{
                            Toast.makeText(this,"Registration Failed : ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                        }

                    }

            }
        }


        val controller= WindowInsetsControllerCompat(window ,window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}
private fun isValidCredentials(Email: String, Password: String): Boolean {
    val emailPattern = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")
    val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$")

    return emailPattern.matches(Email) && passwordPattern.matches(Password)
}