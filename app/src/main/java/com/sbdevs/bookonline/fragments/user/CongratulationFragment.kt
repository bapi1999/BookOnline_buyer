package com.sbdevs.bookonline.fragments.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.databinding.FragmentCongratulationBinding


class CongratulationFragment : Fragment() {
    private var _binding:FragmentCongratulationBinding?= null
    private val binding get() = _binding!!

    private val args: CongratulationFragmentArgs by navArgs()
    private val viewGone = View.GONE
    private val viewVisible = View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCongratulationBinding.inflate(inflater, container, false)
        val warningLayout = binding.warningContainer

        Log.e("layout","onCreateView")

        val warnings:Int = args.warning
        if(warnings == 0){
            warningLayout.visibility = viewGone
        }else{
            warningLayout.visibility = viewVisible
        }


        val orderItem = args.orderItem

        if (orderItem == 0){
            binding.estDateText.visibility = viewGone
        }else{
            binding.estDateText.visibility = viewVisible
        }

        binding.continueToShop.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.orderCounter.text = orderItem.toString()


//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//
//                val intent = Intent(context,MainActivity::class.java)
//                startActivity(intent)
//                activity!!.finish()
//
//            }
//        })




        return binding.root
    }


}