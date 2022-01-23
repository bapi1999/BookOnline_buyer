package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityProductBinding
import com.sbdevs.bookonline.fragments.ProductFragment

class ProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProductBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val extras = intent.getStringExtra("productId").toString()

        val args = Bundle()
        args.putString("productId",extras)

        val productFragment = ProductFragment()
        productFragment.arguments = args

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,productFragment).commit()

    }
}