package com.sbdevs.bookonline.activities.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityMyAddressBinding
import com.sbdevs.bookonline.fragments.ProductFragment
import com.sbdevs.bookonline.fragments.user.MyAddressFragment

class MyAddressActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMyAddressBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment4) as NavHostFragment
//        navController = navHostFragment.findNavController()

        val myAddressFragment= MyAddressFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,myAddressFragment).commit()

    }
}