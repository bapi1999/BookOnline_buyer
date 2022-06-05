package com.sbdevs.bookonline.activities.user

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.user.OrderSummaryAdapter
import com.sbdevs.bookonline.databinding.ActivityProceedOrderBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.user.CartModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.Serializable


class ProceedOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProceedOrderBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private lateinit var continueToPaymentBtn: Button


    lateinit var recyclerView: RecyclerView
    private var receivedList:ArrayList<CartModel> = ArrayList()
    var newReceivedList:ArrayList<CartModel> = ArrayList()
    var outOfStockItemList:ArrayList<CartModel> = ArrayList()

    lateinit var addressList:ArrayList<MutableMap<String, Any>>

    var adapter: OrderSummaryAdapter = OrderSummaryAdapter(receivedList)
    var addressMap:MutableMap<String,Any> = HashMap()

    var amountToPay1: Int = 0
    var amountToPay2: Int = 0

    var discount = 0
    var totalSellingPrice= 0
    var netSellingPrice = 0
    var deliveryCharge = 0L

    var changeInQtyItem: Int = 0

    lateinit var  priceTxt:TextView
    lateinit var  discountTxt:TextView
    lateinit var  deliverChargeTxt:TextView
    lateinit var  amountTxt:TextView

    private val loadingDialog = LoadingDialog()
    private var thereIsAddressError = false

    var fromTo = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProceedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        recyclerView = binding.summerRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        priceTxt = binding.totalLay.totalSellingPrice
        discountTxt = binding.totalLay.totalDiscount
        deliverChargeTxt = binding.totalLay.deliveryCharge
        amountTxt = binding.totalLay.amountToPay
        continueToPaymentBtn = binding.continueToPaymentBtn

        fromTo = intent.getIntExtra("From_To", -1)
        receivedList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>
        newReceivedList.addAll(receivedList)

        amountToPay1 = intent.getIntExtra("total_amount",-1)

        lifecycleScope.launch(Dispatchers.IO){
            calculateThePrice(receivedList)
            delay(500)
            withContext(Dispatchers.Main){
                setValuesInTextView()
            }
            withContext(Dispatchers.IO){
                getAddress()
            }
        }


        adapter = OrderSummaryAdapter(receivedList)
        recyclerView.adapter = adapter
        binding.totalLay.totalItem.text = "( ${receivedList.size} item)"

        binding.changeoraddAddressBtn.setOnClickListener {
            val intent = Intent(this, MyAddressActivity::class.java)
            intent.putExtra("from",2)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }



    }

    override fun onStart() {
        super.onStart()



        continueToPaymentBtn.setOnClickListener {
            val intent = Intent(this, PaymentMethodActivity::class.java)

            if (thereIsAddressError){
                Toast.makeText(this,"Add a nw address before continue",Toast.LENGTH_LONG).show()
            }else{
                continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.purple_500)
                loadingDialog.show(supportFragmentManager,"Show")
                lifecycleScope.launch(Dispatchers.IO) {
                    checkAllOrderMethods(receivedList)
                    delay(1000L)
                    withContext(Dispatchers.Main){
                        intent.putExtra("total_amount",amountToPay2)
                        intent.putExtra("deliveryCharge",deliveryCharge.toInt())
                        intent.putExtra("netSellingPrice",netSellingPrice)
                        intent.putParcelableArrayListExtra("productList",newReceivedList)
                        intent.putParcelableArrayListExtra("OutOfStockProductList",outOfStockItemList)
                        intent.putExtra("address",addressMap as Serializable)
                        intent.putExtra("From_To",fromTo)
                        intent.putExtra("changInQuantity",changeInQtyItem)
                        //todo: 1=> MyCart / 2=> BuyNow
                        startActivity(intent)
                        Log.e("goto PaymentMethod","success")
                        loadingDialog.dismiss()
                    }
                }



            }




        }


    }


    private fun getAddress(){

        val lay2 =  binding.miniAddress

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value, error ->
                error?.let {
                    Toast.makeText(this,"Problem in fetching address",Toast.LENGTH_SHORT).show()
                    Log.e("Get address","${it.message}")

                    binding.addressLay.visibility = View.GONE
                    binding.addressError.visibility = View.VISIBLE
                    thereIsAddressError = true
                    continueToPaymentBtn.text = "No address found"
                    return@addSnapshotListener
                }

                value?.let {
                    val position: Long = it.getLong("select_No")!!
                    val x = it.get("address_list")


                    if (x != null){

                        addressList = x as ArrayList<MutableMap<String, Any>>

                        if (addressList.size != 0){

                            val group:MutableMap<String,Any> = addressList[position.toInt()]
                            addressMap = addressList[position.toInt()]

                            val buyerName:String = group["name"].toString()
                            val buyerAddress1:String = group["address1"].toString()
                            val buyerAddress2:String = group["address2"].toString()
                            val buyerAddressType:String = group["address_type"].toString()
                            val buyerTown:String = group["city_vill"].toString()
                            val buyerPinCode:String = group["pincode"].toString()
                            val buyerState:String = group["state"].toString()
                            val buyerPhone:String = group["phone"].toString()

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

                            binding.addressLay.visibility = View.VISIBLE
                            binding.addressError.visibility = View.GONE
                            thereIsAddressError = false

                        }else{
                            binding.addressLay.visibility = View.GONE
                            binding.addressError.visibility = View.VISIBLE
                            thereIsAddressError = true
                        }

                    }else{
                        binding.addressLay.visibility = View.GONE
                        binding.addressError.visibility = View.VISIBLE
                        thereIsAddressError = true

                    }


                }
            }

    }




    private suspend fun checkAllOrderMethods (list: ArrayList<CartModel>)  {

        for ((i, item) in list.withIndex()) {

            firebaseFirestore.collection("PRODUCTS")
                .document(item.productId).get()
                .addOnSuccessListener {

                    val stockQty = it.getLong("in_stock_quantity")!!.toLong()
                    val orderQuantity = item.orderQuantity
                    val itemSoldSoFar = it.getLong("number_of_item_sold")!!.toLong()
                    when {
                        stockQty >= orderQuantity -> {
                            val newQty = stockQty - orderQuantity
                            val itemSoldNow = itemSoldSoFar + orderQuantity
                            // update product
                            updateProductStock(item.productId, newQty, itemSoldNow)
                            Log.e("checkAllOrderMethods","Size Of RL-${receivedList.size} / NRL-${newReceivedList.size}")


                        }
                        stockQty in 1L until orderQuantity -> {
                            changeInQtyItem ++
                            val newQty = 0L
                            val itemSoldNow = itemSoldSoFar + stockQty

                            updateProductStock(item.productId, newQty, itemSoldNow)
                            newReceivedList[i].orderQuantity = stockQty
                            calculateThePrice(newReceivedList)

                            Log.e("checkAllOrderMethods","Some product Change in Qty")
                        }
                        stockQty <= 0L -> {
//                            Toast.makeText(this, "Some Product just  out of stock now", Toast.LENGTH_SHORT).show()

                            outOfStockItemList.add(item)
                            newReceivedList.remove(item)
                            calculateThePrice(newReceivedList)
                            // Don't update product

                            Log.e("checkAllOrderMethods","Product just OOS. Size Of RL-${receivedList.size} / NRL-${newReceivedList.size}")
                        }
                    }
                }.addOnFailureListener {
                    Log.e("check all orders error","${it.message}")
                }.await()

        }
    }

    private fun updateProductStock(productId: String, newQty: Long, itemSoldNow: Long) {

        val productMap: MutableMap<String, Any> = HashMap()

        productMap["in_stock_quantity"] = newQty
        productMap["number_of_item_sold"] = itemSoldNow

        firebaseFirestore.collection("PRODUCTS").document(productId).set(productMap, SetOptions.merge())
            .addOnSuccessListener { Log.e("updateProductStock ","Success") }
            .addOnFailureListener { Log.e("updateProductStock error","${it.message}") }
    }






    private fun calculateThePrice(list: ArrayList<CartModel> ){

        netSellingPrice = 0
        discount = 0
        totalSellingPrice = 0
        deliveryCharge = 0L
        amountToPay2 = 0

        for ( group  in list){

            val priceOriginal:Long = group.priceOriginal
            val priceSelling:Long = group.priceSelling
            val quantity:Long = group.orderQuantity

            deliveryCharge +=group.deliveryCharge

            discount += if (priceOriginal == 0L){
                0
            }else{
                (priceOriginal.toInt() - priceSelling.toInt())*quantity.toInt()
            }

            netSellingPrice += priceSelling.toInt()*quantity.toInt()

            totalSellingPrice = netSellingPrice+discount


        }

        amountToPay2 = (deliveryCharge+netSellingPrice).toInt()

        Log.e("calculate price","Success")
    }

    private fun setValuesInTextView(){
        priceTxt.text = totalSellingPrice.toString()
        discountTxt.text ="-$discount"
        deliverChargeTxt.text = deliveryCharge.toString()
        amountTxt.text = amountToPay2.toString()

    }

}