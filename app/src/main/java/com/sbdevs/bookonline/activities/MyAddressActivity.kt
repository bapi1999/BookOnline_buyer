package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityMyAddressBinding

class MyAddressActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMyAddressBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment4) as NavHostFragment
        navController = navHostFragment.findNavController()
    }
}