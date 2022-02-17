package com.sbdevs.bookonline.activities.seller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.sbdevs.bookonline.R

class SellerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_dashboard)

        val navController = findNavController(R.id.nav_host_fragment_activity_menu)

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
    }
}