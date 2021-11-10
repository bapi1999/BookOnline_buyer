package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityProductBinding

class ProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProductBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.product_nav_host) as NavHostFragment
        navController = navHostFragment.navController


        navController.setGraph(R.navigation.product_nav_graph, intent.extras)
    }
}