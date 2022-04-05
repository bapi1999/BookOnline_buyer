package com.sbdevs.bookonline.activities.donation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity

class ThanksForDonateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thanks_for_donate)


//        onBackPressedDispatcher.addCallback( object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                val intent = Intent(this@ThanksForDonateActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//                Toast.makeText(requireContext(),"from congratulation fragment", Toast.LENGTH_SHORT).show()
//
//            }
//        })


    }
}