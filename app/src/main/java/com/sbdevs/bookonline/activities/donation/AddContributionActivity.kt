package com.sbdevs.bookonline.activities.donation

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityAddContributionBinding
import com.sbdevs.bookonline.fragments.ProductFragment
import com.sbdevs.bookonline.fragments.user.DonateFragment

class AddContributionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddContributionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContributionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val donateFragment = DonateFragment()
        supportFragmentManager.beginTransaction().replace(R.id.donate_fragment_container,donateFragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}