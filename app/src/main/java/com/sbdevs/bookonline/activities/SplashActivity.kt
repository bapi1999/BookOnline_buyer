package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import android.widget.Toast

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log

import androidx.lifecycle.lifecycleScope

import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.activities.user.RegisterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val myPreference = "ShowLoginPref"
    private val showLoginScreen = "ShowLoginScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences(myPreference, MODE_PRIVATE)
        val show = sharedPreferences.getBoolean(showLoginScreen,true)

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            if (show){
                val loginintent = Intent(this@SplashActivity, RegisterActivity::class.java)
                loginintent.putExtra("from",1)// 1 = from splash/ 2 = from other class
                startActivity(loginintent)
                finish()
            }else{
                val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainintent)
                finish()
            }

        } else {

            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(mainintent)
                    finish()

                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@SplashActivity,e.message,Toast.LENGTH_LONG).show()
                    }
                }


            }

        }


    }

    override fun onStart() {
        super.onStart()
    }

}