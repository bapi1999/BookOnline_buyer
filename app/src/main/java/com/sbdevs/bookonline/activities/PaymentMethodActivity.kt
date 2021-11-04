package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityPaymentMethodBinding

class PaymentMethodActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPaymentMethodBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment5) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.payment_method_navhost, intent.extras)
    }
}