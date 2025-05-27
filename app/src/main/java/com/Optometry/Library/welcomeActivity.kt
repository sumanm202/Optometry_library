package com.Optometry.Library


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.Optometry.Library.databinding.ActivityWelcomeBinding



class welcomeActivity : AppCompatActivity() {
     private val binding:ActivityWelcomeBinding by lazy {
         ActivityWelcomeBinding.inflate(layoutInflater)
     }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


      Handler(Looper.getMainLooper()).postDelayed({
          startActivity(Intent(this,Activity_login_sign_up::class.java))
          finish()
      },5000)
        val controller= WindowInsetsControllerCompat(window ,window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }


    }

