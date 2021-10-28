package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sbdevs.bookonline.R
import android.os.Parcelable
import com.sbdevs.bookonline.databinding.ActivityProceedOrderBinding


class ProceedOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProceedOrderBinding
    private lateinit var navController: NavController
    var list: ArrayList<Parcelable>?  = ArrayList()
    //    var list:ArrayList<CartModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProceedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment3) as NavHostFragment

        navController = navHostFragment.navController
        navController.setGraph(R.navigation.proceed_order_host, intent.extras)

    }
}