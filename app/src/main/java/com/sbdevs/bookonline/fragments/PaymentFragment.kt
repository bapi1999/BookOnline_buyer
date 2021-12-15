package com.sbdevs.bookonline.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentPaymentBinding
import com.sbdevs.bookonline.models.CartModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PaymentFragment : Fragment() {
    private var _binding:FragmentPaymentBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    var recivdList:ArrayList<CartModel> = ArrayList()
    var dbOrderList:ArrayList<MutableMap<String,Any>> = ArrayList()
    var newOrderList:ArrayList<MutableMap<String,Any>> = ArrayList()
    var selecter = 0
    private val  loadingDialog = LoadingDialog()
    var warnings:Int = 0
    // 0 = no warning  1= warning
    var orderedItem :Int = 0

    var boughtProductList: ArrayList<String> = ArrayList()
    private lateinit var payOnline :LinearLayout
    private lateinit var cashOnDelivery :LinearLayout

    private lateinit var address: MutableMap<String, Any>



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                getAllMyOrder()
                getMyBoughtProducts()
            }
            withContext(Dispatchers.Main){
                delay(1000)
            }
        }




        payOnline =  binding.linearLayout
        cashOnDelivery = binding.linearLayout22


        val intent = requireActivity().intent
        val totalAmount = intent.getIntExtra("total_amount", 0)
        binding.totalAmount.text = "Rs. $totalAmount /-"

        address = intent.getSerializableExtra("address") as MutableMap<String, Any>

        val buyerName:String = address["name"].toString()
        val buyerAddress1:String = address["address1"].toString()
        val buyerAddress2:String = address["address2"].toString()
        val buyerAddressType:String = address["address_type"].toString()
        val buyerTown:String = address["city_vill"].toString()
        val buyerPinCode:String = address["pincode"].toString()
        val buyerState:String = address["state"].toString()
        val buyerPhone:String = address["phone"].toString()

        if (buyerName.equals(null) and buyerPhone.equals(null)){
            binding.confirmButton.isEnabled = false
            binding.addressLay.visibility = View.GONE
            binding.noAddress.visibility = View.VISIBLE
        }else{
            binding.confirmButton.isEnabled = true
            binding.addressLay.visibility = View.VISIBLE
            binding.noAddress.visibility = View.GONE
        }

        val addressBuilder  = StringBuilder()
        addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

        val townPinBuilder  = StringBuilder()
        townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

        binding.miniAddress.buyerName.text = buyerName
        binding.miniAddress.buyerAddress.text = addressBuilder.toString()
        binding.miniAddress.buyerAddressType.text = buyerAddressType
        binding.miniAddress.buyerTownAndPin.text =townPinBuilder.toString()
        binding.miniAddress.buyerState.text = buyerState
        binding.miniAddress.buyerPhone.text = buyerPhone

        recivdList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>




        return binding.root

    }

    override fun onStart() {
        super.onStart()


        payOnline.setOnClickListener {
            payOnline.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow)
            cashOnDelivery.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey_400)
            selecter = 1
        }

        cashOnDelivery.setOnClickListener {
            payOnline.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey_400)
            cashOnDelivery.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow)
            selecter = 2
        }

        binding.confirmButton.setOnClickListener {
            when (selecter){
                1 -> {
//
                    // PayTM task and firebase task
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        loadingDialog.dismiss()
                    }
                }
                2 -> {
                    loadingDialog.show(childFragmentManager,"Show")
                    lifecycleScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO){
                            checkAllOrderMethods(recivdList,address)
                            delay(100)
                        }
                        withContext(Dispatchers.IO){
                            updateOrderToBuyer()
                            delay(100)
                            updateMyBoughtProducts()
                            delay(100)
                            deleteProductFromCatr()
                        }
                        delay(2700)
                        withContext(Dispatchers.Main){
                            loadingDialog.dismiss()
                            val action = PaymentFragmentDirections.actionPaymentFragmentToCongratulationFragment(warnings,orderedItem)
                            findNavController().navigate(action)
                        }

                    }

                }
                else ->{
                    loadingDialog.dismiss()
                    Toast.makeText(context,"Select payment method",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun checkAllOrderMethods(list: ArrayList<CartModel>, address: MutableMap<String, Any>) = CoroutineScope(Dispatchers.IO).launch{
        for (item in list){
            firebaseFirestore.collection("PRODUCTS")
                .document(item.productId).get()
                .addOnSuccessListener {

                val stockQty = it.getLong("in_stock_quantity")!!.toLong()
                val docname:String = generateDocName()
                val orderQuantity = item.orderQuantity
                val sellerOrderMap:MutableMap<String,Any> = HashMap()

                when {
                    stockQty >= orderQuantity -> {
                        val newQty = stockQty - orderQuantity
                        orderedItem ++
                        // update product
                        updateProductStock(item.productId,newQty)
                        // create order
                        createOrderToSeller(item.url,item.title,item.productId,item.sellerId,orderQuantity,docname,address,item.priceSelling)
                        notifySeller(item.url,item.title,orderQuantity,docname,item.sellerId)
                        //update MyOrder
                        sellerOrderMap["orderID"] = docname
                        sellerOrderMap["sellerId"] = item.sellerId
                        newOrderList.add(sellerOrderMap)
                        boughtProductList.add(item.productId)


                    }
                    stockQty  in 1 until orderQuantity -> {
                        orderedItem ++
                        val newQty = 0L


                        // update product
                        updateProductStock(item.productId,newQty)
                        // create order
                        createOrderToSeller(item.url,item.title,item.productId,item.sellerId,stockQty,docname,address,item.priceSelling)
                        notifySeller(item.url,item.title,orderQuantity,docname,item.sellerId)
                        //update MyOrder
                        sellerOrderMap["orderID"] = docname
                        sellerOrderMap["sellerId"] = item.sellerId
                        newOrderList.add(sellerOrderMap)
                        boughtProductList.add(item.productId)

                    }
                    stockQty == 0L -> {
                        Toast.makeText(context,"Some Product just got out of stock now",Toast.LENGTH_SHORT).show()
                        warnings = 1
                        // Don't update product
                        // Don't create order
                    }
                }
            }.await()
        }
    }

    private fun updateProductStock(productId:String, newQty:Long){

        val productMap:MutableMap<String,Any> = HashMap()

        productMap["in_stock_quantity"] = newQty

        firebaseFirestore.collection("PRODUCTS").document(productId).update(productMap)
    }

    private fun createOrderToSeller(thumbnail:String, title:String, productId:String,
                                    sellerId:String, orderQuantity:Long, docName:String
                                    , address:MutableMap<String,Any>
                                    , priceSelling:Long) = CoroutineScope(Dispatchers.IO).launch{
        val productMap:MutableMap<String,Any> = HashMap()
        productMap["productThumbnail"] = thumbnail
        productMap["productTitle"] = title
        productMap["productId"] = productId.trim()
        productMap["price"] = priceSelling
        productMap["buyerId"] = user!!.uid
        productMap["ordered_Qty"] = orderQuantity
        productMap["tracKingId"] = "No Available yet"
        productMap["status"] = "new" //0 for new
        //todo- All status must be in lowercase
        productMap["orderTime"] = FieldValue.serverTimestamp()
        productMap["address"] = address



        firebaseFirestore.collection("USERS").document(sellerId)
            .collection("SELLER_DATA")
            .document("SELLER_DATA").collection("ORDERS")
            .document(docName).set(productMap).await()

    }

    private fun updateOrderToBuyer(){



        val updateOrderMap:MutableMap<String,Any> = HashMap()
        updateOrderMap["order_list"] = newOrderList

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ORDERS").update(updateOrderMap)

    }

    private fun getMyBoughtProducts(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA").document("THINGS_I_BOUGHT").get()
            .addOnSuccessListener {
                val x = it.get("my_bought_items")

                if (x!=null){
                    boughtProductList = x as java.util.ArrayList<String>

                }else{
                    Log.d(TAG,"blank")
                }
            }
    }

    private fun updateMyBoughtProducts(){
        val boughtMap:MutableMap<String,Any> = HashMap()
        boughtMap["my_bought_items"] = boughtProductList
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA").document("THINGS_I_BOUGHT").update(boughtMap)
            .addOnSuccessListener {
            }
    }

    private fun getAllMyOrder(){
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ORDERS").get().addOnSuccessListener {
                val x = it.get("order_list")

                if (x != null){
                    dbOrderList = x as ArrayList<MutableMap<String,Any>>
                    newOrderList.addAll(dbOrderList)

                }else{
                    Log.d("MyOrder","No order foung")
                }
            }.addOnFailureListener {
                Toast.makeText(context,it.message.toString(),Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteProductFromCatr(){

        val updates = hashMapOf<String, Any>(
            "cart_list" to FieldValue.delete()
        )
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA").document("MY_CART").update(updates).addOnSuccessListener {

            }
    }

    private fun generateDocName():String{

        val timeString = LocalDateTime.now().toString()
        val userString = user!!.uid.toString().substring(0,5)
        val randomString:String = UUID.randomUUID().toString().substring(0,5)
        val docBuilder:StringBuilder = StringBuilder()
        docBuilder.append(timeString).append(userString).append(randomString)
        val docName = docBuilder.toString().replace(".","_").replace("-","_").replace(":","_")
        return docName
    }

    private fun notifySeller(thumbnail:String, title:String,
                                              orderQuantity:Long, docName:String,sellerId:String){

        val notificationMap:MutableMap<String,Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["title"] ="You got a new order"
        notificationMap["description"] ="$orderQuantity $title has been ordered"
        notificationMap["image"] = thumbnail
        notificationMap["order_id"] = docName



        firebaseFirestore.collection("USERS").document(sellerId)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("NOTIFICATION").add(notificationMap)
            .addOnSuccessListener {
                Log.i("Notify Seller","successful")
            }
            .addOnFailureListener {
                Log.e("Notify Seller","${it.message}")
            }






    }




}