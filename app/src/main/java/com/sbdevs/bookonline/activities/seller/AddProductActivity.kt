package com.sbdevs.bookonline.activities.seller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityAddProductBinding


class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.add_product_host_fragment)



    }





}