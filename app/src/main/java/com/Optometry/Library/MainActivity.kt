package com.Optometry.Library

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.Optometry.Library.Adapters.HomeAdapter
import com.Optometry.Library.Adapters.LAYOUT_BOD
import com.Optometry.Library.Adapters.LAYOUT_HOME
import com.Optometry.Library.Models.BooksModel
import com.Optometry.Library.Models.HomeModel
import com.Optometry.Library.Repository.MainRepo
import com.Optometry.Library.Utils.MyResponses
import com.Optometry.Library.Utils.SpringScrollHelper
import com.Optometry.Library.Utils.loadBannerAd
import com.Optometry.Library.Utils.removeWithAnim
import com.Optometry.Library.Utils.showWithAnim
import com.Optometry.Library.ViewModels.MainViewModel
import com.Optometry.Library.ViewModels.MainViewModelFactory
import com.Optometry.Library.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {


    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE


    lateinit var binding: ActivityMainBinding
    val activity = this
    val list: ArrayList<HomeModel> = ArrayList()
    val adapter = HomeAdapter(list, activity)
    private val TAG = "MainActivity"
    private val repo = MainRepo(activity)
    private val viewModel by lazy {
        ViewModelProvider(activity, MainViewModelFactory(repo))[MainViewModel::class.java]
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdates()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.apply {
            mBannerAd.loadBannerAd()
            mRvHome.adapter = adapter
            SpringScrollHelper().attachToRecyclerView(mRvHome)
            viewModel.getHomeData()
            handleHomeBackend()

            mErrorLayout.mTryAgainBtn.setOnClickListener {
                viewModel.getHomeData()
            }

        }


    }

    private fun handleHomeBackend() {
        viewModel.homeLiveData.observe(activity) {
            when (it) {
                is MyResponses.Error -> {
                    Log.i(TAG, "handleHomeBackend: ${it.errorMessage}")
                    binding.mErrorHolder.showWithAnim()
                    binding.mLoaderHolder.removeWithAnim()
                }
                is MyResponses.Loading -> {
                    Log.i(TAG, "handleHomeBackend: Loading...")
                    binding.mErrorHolder.removeWithAnim()
                    binding.mLoaderHolder.showWithAnim()
                }
                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    binding.mLoaderHolder.removeWithAnim()
                    val tempList = it.data
                    list.clear()
                    Log.i(TAG, "handleHomeBackend: Success Called $tempList ")
                    tempList?.forEach {
                        list.add(it)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.contactus_nav -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data =
                    Uri.parse("mailto:optometrylibrary@gmail.com?subject=Query")
                startActivity(emailIntent)
                true
            }

            R.id.disclaimer_nav -> {
                // Handle click on diagnostic_procedures item

                true // Return true to indicate that the item click has been handled
            }

            R.id.t_and_c_nav -> {

                // Open Terms & Conditions link in the browser
                val termsConditionsUrl =
                    "https://optometrylibrary.blogspot.com/2020/08/terms-conditions-by-downloading-or.html?m=1"
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(termsConditionsUrl))
                startActivity(browserIntent)
                true
            }

            R.id.signout_nav -> {
                // Handle sign-out action here
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(this, gso)

                googleSignInClient.signOut().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val auth = FirebaseAuth.getInstance()
                        if (auth.currentUser != null) {
                            auth.signOut()
                            startActivity(Intent(this, Activity_login_sign_up::class.java))
                            finish()
                        }
                    } else {
                        // Handle sign-out failure
                    }
                }
                true // Return true to indicate that the item click has been handled
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkForAppUpdates(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val  isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    100000
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100000){
            if (resultCode != RESULT_OK){
                println("Something went wrong updating...")
            }
        }
    }
}
