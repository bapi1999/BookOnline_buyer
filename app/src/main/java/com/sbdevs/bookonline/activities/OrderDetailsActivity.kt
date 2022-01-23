package com.sbdevs.bookonline.activities

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityOrderDetailsBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.othercalss.FireStoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOrderDetailsBinding
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var productImage:ImageView

    private lateinit var productId:String

    private lateinit var orderID:String
    private lateinit var sellerID:String

    private var isEligibleForRating= false

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderID = intent.getStringExtra("orderID")!!
        sellerID = intent.getStringExtra("sellerID")!!

        loadingDialog.show(supportFragmentManager,"show")

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                getMyOrder(orderID,sellerID)
            }
        }


        productImage = binding.lay1.productImage






    }

    override fun onStart() {
        super.onStart()

        binding.cancelOrderBtn.setOnClickListener {
            cancelOrder(orderID,sellerID)
        }
        binding.returnOrderBtn.setOnClickListener {
            returnOrder(orderID,sellerID)
        }

    }

    private fun getMyOrder(orderID:String, sellerID:String)= CoroutineScope(Dispatchers.IO).launch {

        val lay1 = binding.lay1
        val lay2 = binding.lay2

        val orderRef = firebaseFirestore.collection("USERS").document(sellerID)
            .collection("SELLER_DATA")
            .document("SELLER_DATA").collection("ORDERS")
            .document(orderID)

        orderRef.addSnapshotListener {value,error->
            error?.let {
                Toast.makeText(this@OrderDetailsActivity,"Fail to load", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            value?.let{
                val productThumbnail= it.get("productThumbnail").toString().trim()
                val title=it.get("productTitle").toString()
                val price = it.get("price").toString()
                val orderedQty = it.getLong("ordered_Qty")!!
                val status = it.get("status").toString()

                val isCanceled = it.getBoolean("is_order_canceled")!!


                val productIdDB = it.get("productId").toString()
                val tracKingId = it.get("tracKingId").toString()

                val orderTime = it.getTimestamp("Time_ordered")!!.toDate()

                val buyerId = it.get("buyerId").toString()
                val address:MutableMap<String,Any> = it.get("address") as MutableMap<String,Any>

                val daysAgo = FireStoreData().msToTimeAgo(this@OrderDetailsActivity,orderTime)

                productId = productIdDB

                when(status){
                    "new","accepted" ->{
                        binding.cancelOrderBtn.isEnabled = true
                        binding.cancelOrderBtn.visibility = View.VISIBLE
                        binding.returnOrderBtn.visibility = View.GONE
                    }
                    "shipped" ->{
                        binding.cancelOrderBtn.isEnabled = false
                        binding.cancelOrderBtn.visibility = View.VISIBLE
                        binding.returnOrderBtn.visibility = View.GONE
                    }
                    "delivered" ->{
                        binding.cancelOrderBtn.visibility = View.GONE
                        binding.returnOrderBtn.visibility = View.VISIBLE
                    }
                    "canceled"->{
                        binding.cancelOrderBtn.visibility = View.GONE
                        binding.returnOrderBtn.visibility = View.GONE
                    }
                    "returned"->{
                        binding.cancelOrderBtn.visibility = View.GONE
                        binding.returnOrderBtn.visibility = View.GONE
                    }
                }
                //todo- All status must be in lowercase



                binding.statusTxt.text = status
                binding.orderIdTxt.text = orderID
                binding.trakingIdTxt.text = tracKingId
                binding.orderedDateText.text = daysAgo


                Glide.with(this@OrderDetailsActivity).load(productThumbnail)
                    .placeholder(R.drawable.as_square_placeholder)
                    .into(productImage)

                lay1.titleTxt.text = title
                lay1.priceTxt.text = price
                lay1.productQuantity.text = orderedQty.toString()



                val buyerName:String = address["name"].toString()
                val buyerAddress1:String = address["address1"].toString()
                val buyerAddress2:String = address["address2"].toString()
                val buyerAddressType:String = address["address_type"].toString()


                val buyerTown:String = address["city_vill"].toString()
                val buyerPinCode:String = address["pincode"].toString()

                val buyerState:String = address["state"].toString()
                val buyerPhone:String = address["phone"].toString()

                val addressBuilder  = StringBuilder()
                addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

                val townPinBuilder  = StringBuilder()
                townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

                lay2.buyerName.text = buyerName
                lay2.buyerAddress.text = addressBuilder.toString()
                lay2.buyerAddressType.text = buyerAddressType
                lay2.buyerTownAndPin.text =townPinBuilder.toString()
                lay2.buyerState.text = buyerState
                lay2.buyerPhone.text = buyerPhone

                loadingDialog.dismiss()

            }

        }



    }

    private fun cancelOrder(orderID:String, sellerID:String){
        val cancelMap:MutableMap<String,Any> = HashMap()
        cancelMap["status"] = "canceled"
        cancelMap["time_cancellation_request"] = FieldValue.serverTimestamp()

        val orderRef = firebaseFirestore.collection("USERS").document(sellerID)
            .collection("SELLER_DATA")
            .document("5_ALL_ORDERS").collection("ORDER")
            .document(orderID)
        orderRef.update(cancelMap)
    }

    private fun returnOrder(orderID:String, sellerID:String){
        val returnMap:MutableMap<String,Any> = HashMap()
        returnMap["status"] = "returned"
        returnMap["time_returned_request"] = FieldValue.serverTimestamp()

        val orderRef = firebaseFirestore.collection("USERS").document(sellerID)
            .collection("SELLER_DATA")
            .document("5_ALL_ORDERS").collection("ORDER")
            .document(orderID)
        orderRef.update(returnMap)
    }

}