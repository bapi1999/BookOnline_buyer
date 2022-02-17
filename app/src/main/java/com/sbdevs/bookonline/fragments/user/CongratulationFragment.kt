package com.sbdevs.bookonline.fragments.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.databinding.FragmentCongratulationBinding


class CongratulationFragment : Fragment() {
    private var _binding:FragmentCongratulationBinding?= null
    private val binding get() = _binding!!

    private val args: CongratulationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCongratulationBinding.inflate(inflater, container, false)

        val warnings:Int = args.warning
        val orderItem = args.orderItem
        val warningLayout = binding.warningContainer
        val viewGone = View.GONE
        val viewVisible = View.VISIBLE

        if(warnings == 0){
            warningLayout.visibility = viewGone
        }else{
            warningLayout.visibility = viewVisible
        }

        if (orderItem == 0){
            binding.estDateText.visibility = viewGone
        }else{
            binding.estDateText.visibility = viewVisible
        }

        binding.continueToShop.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }

        binding.orderCounter.text = orderItem.toString()


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val intent = Intent(context,MainActivity::class.java)
                startActivity(intent)
                activity!!.finish()

            }
        })




        return binding.root
    }


}