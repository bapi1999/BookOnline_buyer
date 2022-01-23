package com.sbdevs.bookonline.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.ProductImgAdapter
import com.sbdevs.bookonline.adapters.ProductZoomImageAdapter
import com.sbdevs.bookonline.databinding.FragmentPaymentBinding
import com.sbdevs.bookonline.databinding.FragmentProductImageBinding

class ProductImageFragment : Fragment() {
    private var _binding :FragmentProductImageBinding? = null
    private val binding get() = _binding!!
    private lateinit var productImgViewPager:ViewPager2



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductImageBinding.inflate(inflater, container, false)

        productImgViewPager = binding.imageZoomPager

        val productImgList = arguments?.getStringArrayList("image_list")

        val adapter = productImgList?.let { ProductZoomImageAdapter(it) }
        productImgViewPager.adapter = adapter
        binding.dotsIndicator.setViewPager2(productImgViewPager)

        return binding.root
    }

}