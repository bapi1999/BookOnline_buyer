package com.sbdevs.bookonline.seller.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.RegisterActivity
import com.sbdevs.bookonline.databinding.ActivitySlSplashBinding

class SlSplashActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySlSplashBinding

    private val firebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }

    override fun onStart() {
        super.onStart()

        if (Firebase.auth.currentUser == null) {
            val loginintent = Intent(this, RegisterActivity::class.java)
            startActivity(loginintent)
            finish()
        } else {
            checkIsSeller()
        }

    }


    private fun checkIsSeller(){
        firebaseFirestore.collection("USERS")
            .document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                val isSeller:Boolean = it.getBoolean("Is_seller")!!
                if (isSeller){

                    val mainintent = Intent(this, SellerMainActivity::class.java)
                    startActivity(mainintent)
                    finish()

                }else{
                    val newRegisterIntent = Intent(this, SellerRegisterActivity::class.java)
                    startActivity(newRegisterIntent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error in getting user details",Toast.LENGTH_LONG).show()
                Log.e("checkIsSeller error","${it.message}")
                finish()
            }
    }
}