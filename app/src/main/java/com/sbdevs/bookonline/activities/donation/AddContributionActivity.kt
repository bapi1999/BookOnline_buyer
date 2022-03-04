package com.sbdevs.bookonline.activities.donation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.fragments.ProductFragment
import com.sbdevs.bookonline.fragments.user.DonateFragment

class AddContributionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contribution)

        val donateFragment = DonateFragment()
        supportFragmentManager.beginTransaction().replace(R.id.donate_fragment_container,donateFragment).commit()
    }
}