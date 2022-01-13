package com.sbdevs.bookonline.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityAddAddressBinding
import com.sbdevs.bookonline.fragments.MyAddressFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class AddAddressActivity : AppCompatActivity() {
    lateinit var binding:ActivityAddAddressBinding

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth =FirebaseAuth.getInstance()
    lateinit var buyerName:TextInputLayout
    lateinit var buyerPhone:TextInputLayout
    lateinit var buyerPincode:TextInputLayout
    lateinit var buyerAddress1:TextInputLayout
    lateinit var buyerAddress2:TextInputLayout
    lateinit var buyerTown:TextInputLayout
    lateinit var buyerState:TextInputLayout
    lateinit var buyerAddressType:TextInputLayout
    lateinit var autoCompleteType:AutoCompleteTextView
    lateinit var autoCompleteState:AutoCompleteTextView

    lateinit var loadingDialog : Dialog

    var list:ArrayList<MutableMap<String,Any>> = ArrayList()
//    lateinit var buyerName:TextInputLayout

    override fun onResume() {
        super.onResume()

        autoCompleteType = binding.lay1.autoCompleteType
        val addressTypelist = resources.getStringArray(R.array.address_type)
        val typeAdapter = ArrayAdapter(this,R.layout.le_dropdown_item,addressTypelist)
        autoCompleteType.setAdapter(typeAdapter)

        autoCompleteState = binding.lay1.autoCompleteState
        val stateList = resources.getStringArray(R.array.india_states)
        val sateAdapter = ArrayAdapter(this,R.layout.le_dropdown_item,stateList)
        autoCompleteState.setAdapter(sateAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(this,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            getAddressList()
        }



        buyerName = binding.lay1.buyerName
        buyerPhone = binding.lay1.buyerPhone
        buyerPincode = binding.lay1.buyerPincode
        buyerAddress1 = binding.lay1.buyerAddress1
        buyerAddress2 = binding.lay1.buyerAddress2
        buyerTown = binding.lay1.buyerTown
        buyerState =binding.lay1.buyerState
        buyerAddressType = binding.lay1.buyerAddressType

        autoCompleteType = binding.lay1.autoCompleteType
        autoCompleteState = binding.lay1.autoCompleteState



        val from= intent.getIntExtra("from",0)
        // todo:  1=> Add new address / 2=> Edit address
        if (from == 1){
//            Toast.makeText(this,"New address", Toast.LENGTH_SHORT).show()
        }else if(from == 2){
            Toast.makeText(this,"edit address", Toast.LENGTH_SHORT).show()
            val intent = intent.getSerializableExtra("editMap")
            val group:MutableMap<String,Any> = intent as MutableMap<String, Any>

            val buyerNameString:String = group["name"].toString()
            val buyerAddress1String:String = group["address1"].toString()
            val buyerAddress2String:String = group["address2"].toString()
            val buyerAddressTypeString:String = group["address_type"].toString()


            val buyerTownString:String = group["city_vill"].toString()
            val buyerPinCodeString:String = group["pincode"].toString()

            val buyerStateString:String = group["state"].toString()
            val buyerPhoneString:String = group["phone"].toString()
            buyerName.editText!!.setText(buyerNameString)

        }else{
            Toast.makeText(this,"baal", Toast.LENGTH_SHORT).show()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.addNewAddress.setOnClickListener {
            loadingDialog.show()
            checkAllDetails()
        }




    }

    private fun checkName(): Boolean {
        val nameInput: String = buyerName.editText?.text.toString()
        return if (nameInput.isEmpty()) {
            buyerName.isErrorEnabled = true
            buyerName.error = "Field can't be empty"
            false
        } else {
//            buyerName.isErrorEnabled = false
            buyerName.error = null
            true

        }
    }

    private fun checkMobile(): Boolean {
        val mobileInput: String = buyerPhone.editText?.text.toString()
        return if (mobileInput.isEmpty()) {
            buyerPhone.isErrorEnabled = true
            buyerPhone.error = "Field can't be empty"
            false
        } else {
            if(mobileInput.length==10){
//                buyerPhone.isErrorEnabled = false
                buyerPhone.error = null
                true
            }else{
                buyerPhone.isErrorEnabled = true
                buyerPhone.error = "Must be 10 digit number"
                false
            }

        }
    }
    private fun checkPincode(): Boolean {
        val pincodeInput: String = buyerPincode.editText?.text.toString()
        return if (pincodeInput.isEmpty()) {
            buyerPincode.isErrorEnabled = true
            buyerPincode.error = "Field can't be empty"
            false
        } else {

            if(pincodeInput.length==6){
//                buyerPincode.isErrorEnabled = false
                buyerPincode.error = null
                true
            }else{
                buyerPincode.isErrorEnabled = true
                buyerPincode.error = "Must be 6 digit number"
                false
            }



        }
    }
    private fun checkAddress1(): Boolean {
        val address1Input: String = buyerAddress1.editText?.text.toString()
        return if (address1Input.isEmpty()) {
            buyerAddress1.isErrorEnabled = true
            buyerAddress1.error = "Field can't be empty"
            false
        } else {
//            buyerAddress1.isErrorEnabled = false
            buyerAddress1.error = null
            true

        }
    }

    private fun checkAddress2(): Boolean {
        val address2Input: String = buyerAddress2.editText?.text.toString()
        return if (address2Input.isEmpty()) {
            buyerAddress2.isErrorEnabled = true
            buyerAddress2.error = "Field can't be empty"
            false
        } else {
//            buyerAddress2.isErrorEnabled = false
            buyerAddress2.error = null
            true

        }
    }

    private fun checkTown(): Boolean {
        val townInput: String = buyerTown.editText?.text.toString()
        return if (townInput.isEmpty()) {
            buyerTown.isErrorEnabled = true
            buyerTown.error = "Field can't be empty"
            false
        } else {
//            buyerTown.isErrorEnabled = false
            buyerTown.error = null
            true

        }
    }

    private fun checkState(): Boolean {
        val stateInput: String = autoCompleteState.text.toString()
        return if (stateInput.isEmpty()) {
            buyerState.isErrorEnabled = true
            buyerState.error = "Select an item"
            false
        } else {
//            buyerState.isErrorEnabled = false
            buyerState.error = null
            true

        }
    }

    private fun checkType(): Boolean {
        val typeInput: String = autoCompleteType.text.toString()
        return if (typeInput.isEmpty()) {
            buyerAddressType.isErrorEnabled = true
            buyerAddressType.error = "Select an item"
            false
        } else {
//            buyerAddressType.isErrorEnabled = false
            buyerAddressType.error = null
            true

        }
    }


    fun getAddressList()  = CoroutineScope(Dispatchers.IO).launch{
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("address_list")
                    if (x != null){
                        list = x as ArrayList<MutableMap<String, Any>>
                    }
                }
            }.await()
    }

    private fun checkAllDetails() {
        if (!checkName() or !checkMobile() or !checkPincode() or !checkAddress1() or !checkAddress2()
            or !checkTown() or !checkState() or !checkType() ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        } else {
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val valueMap:MutableMap<String,Any> = HashMap()

                    valueMap["address1"] = buyerAddress1.editText?.text.toString()
                    valueMap["address2"] =  buyerAddress2.editText?.text.toString()
                    valueMap["address_type"] =autoCompleteType.text.toString()
                    valueMap["name"] = buyerName.editText?.text.toString()
                    valueMap["phone"] = buyerPhone.editText?.text.toString()
                    valueMap["pincode"] =buyerPincode.editText?.text.toString()
                    valueMap["state"] = autoCompleteState.text.toString()
                    valueMap["city_vill"] =buyerTown.editText?.text.toString()

                    list.add(valueMap)
                    val addressMap:MutableMap<String,Any> = HashMap<String,Any>()
                    addressMap["address_list"] = list
                    firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
                        .collection("USER_DATA").document("MY_ADDRESSES")
                        .update(addressMap).addOnCompleteListener {
                            loadingDialog.dismiss()
                            finish()
                        }

                }catch (e:Exception){
                    withContext(Dispatchers.Main){

                        Toast.makeText(this@AddAddressActivity,e.message,Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }



}