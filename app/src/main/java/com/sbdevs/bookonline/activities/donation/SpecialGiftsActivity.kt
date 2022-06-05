package com.sbdevs.bookonline.activities.donation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityAddContributionBinding
import com.sbdevs.bookonline.databinding.ActivitySpecialGiftsBinding

class SpecialGiftsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialGiftsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialGiftsBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}