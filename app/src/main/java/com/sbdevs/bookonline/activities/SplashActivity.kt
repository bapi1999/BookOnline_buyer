package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import android.widget.Toast

import android.content.Intent
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val loginintent = Intent(this@SplashActivity, RegisterActivity::class.java)
            startActivity(loginintent)
            finish()
        } else {

            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(mainintent)
                    finish()

//                    FirebaseFirestore.getInstance().collection("USERS").document(currentUser.uid)
//                        .update("Last seen", FieldValue.serverTimestamp()).await()
//                    withContext(Dispatchers.Main){
//
//                    }
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