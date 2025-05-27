package com.Optometry.Library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.Optometry.Library.databinding.ActivityLoginSignUpBinding
import com.Optometry.Library.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class Activity_login_sign_up : AppCompatActivity() {
    private val binding:ActivityLoginSignUpBinding by lazy {
        ActivityLoginSignUpBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onStart() {
        super.onStart()
        //check if user already logged in
        val currentUser: FirebaseUser?=auth.currentUser
        if (currentUser!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()


        googleSignInClient=GoogleSignIn.getClient(this,gso)


        //Initialize Firebase Auth
        auth= FirebaseAuth.getInstance()


        binding.Forgetpassword.setOnClickListener {
            startActivity(Intent(this,PasswordResetActivity::class.java))
            finish()
        }

        binding.LoginBtn.setOnClickListener {
            val LoginEmail=binding.LoginEmail.text.toString().trim()
            val LoginPassword=binding.LoginPassword.text.toString().trim()

            if (LoginEmail.isEmpty()||LoginPassword.isEmpty()){
                Toast.makeText(this,"Please Fill All The Details",Toast.LENGTH_SHORT).show()
            }else if (isValid(LoginEmail,LoginPassword)==true){
                auth.signInWithEmailAndPassword(LoginEmail,LoginPassword)
                    .addOnCompleteListener { task->
                        if (task.isSuccessful){
                            Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this,"Login Failed Enter Correct Email or Password : ${task.exception?.message}",Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }


        binding.googleloginBtn.setOnClickListener {
            val signInClient=googleSignInClient.signInIntent
            launcher.launch(signInClient)


        }
        binding.phloginBtn.setOnClickListener {
            startActivity(Intent(this,phLoginAvtivity::class.java))
            finish()
        }

        binding.GoToSignUp.setOnClickListener {
            startActivity(Intent(this,Activity_Signup::class.java))
            finish()
        }


        val controller= WindowInsetsControllerCompat(window ,window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

    }
    private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        result->
        if (result.resultCode==Activity.RESULT_OK){
            val task=GoogleSignIn.getSignedInAccountFromIntent(result.data)

            if (task.isSuccessful){
                val account:GoogleSignInAccount?=task.result
                val credential=GoogleAuthProvider.getCredential(account?.idToken,null)
                auth.signInWithCredential(credential).addOnCompleteListener {

                    if (it.isSuccessful){
                        Toast.makeText(this,"Done",Toast.LENGTH_LONG).show()
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }else{
            Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
        }
    }
}
fun isValid(LoginEmail: String, LoginPassword: String): Boolean {
    val emailPattern = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")
    val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$")

    return emailPattern.matches(LoginEmail) && passwordPattern.matches(LoginPassword)
}
