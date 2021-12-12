package com.sbdevs.bookonline.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentLoadingDialogBinding

class LoadingDialog : DialogFragment() {
    private var _binding:FragmentLoadingDialogBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)

        isCancelable = false
        return binding.root
    }

}