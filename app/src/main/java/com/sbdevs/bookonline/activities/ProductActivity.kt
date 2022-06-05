package com.sbdevs.bookonline.activities

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityProductBinding
import com.sbdevs.bookonline.fragments.ProductFragment
import com.sbdevs.bookonline.seller.fragment.SlProductDetailsFragment

class ProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val extras = intent.getStringExtra("productId").toString()
        val isSeller = intent.getBooleanExtra("is_Seller",false)

        val args = Bundle()
        args.putString("productId",extras)

        val productFragment = if (isSeller){
          SlProductDetailsFragment()
        }else{
             ProductFragment()
        }

        productFragment.arguments = args

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,productFragment).commit()

    }
}