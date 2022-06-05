package com.sbdevs.bookonline.fragments.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
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

        Log.e("layout","onCreateView")


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


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val intent = Intent(context,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                activity!!.finish()

            }
        })




        return binding.root
    }


}