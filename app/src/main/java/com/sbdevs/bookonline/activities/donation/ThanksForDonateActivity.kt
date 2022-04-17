package com.sbdevs.bookonline.activities.donation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.databinding.ActivityThanksForDonateBinding

class ThanksForDonateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityThanksForDonateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanksForDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newlyAddedPoint:Int = intent.getIntExtra("totalPoint",0)

        binding.newlyAddedPoint.text = "$newlyAddedPoint Points"



    }
}