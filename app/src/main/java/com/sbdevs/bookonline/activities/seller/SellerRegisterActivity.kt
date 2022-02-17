package com.sbdevs.bookonline.activities.seller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivitySellerRegisterBinding

class SellerRegisterActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.register_host_fragment)

    }

}