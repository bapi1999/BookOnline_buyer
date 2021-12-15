package com.sbdevs.bookonline.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentPaymentBinding
import com.sbdevs.bookonline.databinding.FragmentProductImageBinding

class ProductImageFragment : Fragment() {
    private var _binding :FragmentProductImageBinding? = null
    private val binding get() = _binding!!

    private val args:ProductImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductImageBinding.inflate(inflater, container, false)

        val url = args.productUrl
        val imageView = binding.touchImageView

        Glide.with(requireContext()).load(url).placeholder(R.drawable.as_square_placeholder).into(imageView)

        return binding.root
    }

}