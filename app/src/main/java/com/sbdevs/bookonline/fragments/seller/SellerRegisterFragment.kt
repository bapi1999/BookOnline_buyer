package com.sbdevs.bookonline.fragments.seller

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentSellerRegisterBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SellerRegisterFragment : Fragment() {
    private var _binding : FragmentSellerRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var termAndPolicyBox:CheckBox
    private lateinit var privacyPolicyBox:CheckBox
    private lateinit var returnPolicyBox:CheckBox

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val loadingDialog = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerRegisterBinding.inflate(inflater,container, false)

        termAndPolicyBox = binding.checkBox6
        privacyPolicyBox = binding.checkBox8
        returnPolicyBox = binding.checkBox9

        binding.button4.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllBox()
        }


        return binding.root
    }

    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_600) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkPrivacyPolicyBox(): Boolean {
        return if (privacyPolicyBox.isChecked) {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_600) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }
    private fun checkReturnPolicyBox(): Boolean {
        return if (returnPolicyBox.isChecked) {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_600) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkAllBox (){
        if (!checkTermsAndPolicyBox() or !checkPrivacyPolicyBox() or !checkReturnPolicyBox()){
            Toast.makeText(requireContext(),"check all box",Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                createPaths()
            }

        }
    }

    private suspend  fun createPaths(){


        val timstamp1 = FieldValue.serverTimestamp()

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["Is_seller"] = true
        userMap["seller_register_date"] = timstamp1

        val sellerDataMap: MutableMap<String, Any> = HashMap()
        sellerDataMap["new_notification"] = timstamp1

        val earningMap: MutableMap<String, Any> = HashMap()
        earningMap["current_amount"] = 0L

        val businessDetailsMap: MutableMap<String, Any> = HashMap()
        businessDetailsMap["Business_name"] = ""
        businessDetailsMap["Business_type"] = ""
        businessDetailsMap["Is_BusinessDetail_Added"] = false

        val bankDetailsMap: MutableMap<String, Any> = HashMap()
        bankDetailsMap["Bank_account_number"] = ""
        bankDetailsMap["Bank_ifsc_code"] = ""
        //bankDetailsMap["account_holder_name"]=""
        bankDetailsMap["UPI_Type"] = ""
        bankDetailsMap["UPI_id"] =""
        bankDetailsMap["Is_BankDetail_Added"] = false

        val dummyMap: MutableMap<String, Any> = HashMap()
        dummyMap["DUMMY"] = "dummy"

        val currentUser = firebaseAuth.currentUser!!.uid

        firebaseFirestore.collection("USERS").document(currentUser).update(userMap).await()

        val docRef = firebaseFirestore.collection("USERS")
            .document(currentUser).collection("SELLER_DATA")

        docRef.document("BANK_DETAILS").set(bankDetailsMap).await()
        docRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
        docRef.document("MY_EARNING").set(earningMap).await()

        val sellerRef = docRef.document("SELLER_DATA")

        sellerRef.set(sellerDataMap).await()
        sellerRef.collection("EARNINGS").document("DUMMY").set(dummyMap).await()

        withContext(Dispatchers.Main){
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_SHORT).show()

            val action = SellerRegisterFragmentDirections.actionSellerRegisterFragmentToAddBusinessDetailsFragment()
            findNavController().navigate(action)
            loadingDialog.dismiss()
        }


    }

}