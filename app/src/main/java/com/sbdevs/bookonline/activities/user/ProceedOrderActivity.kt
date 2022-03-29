package com.sbdevs.bookonline.activities.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.bookonline.R
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.user.OrderSummaryAdapter
import com.sbdevs.bookonline.databinding.ActivityProceedOrderBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.user.CartModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable


class ProceedOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProceedOrderBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private lateinit var continueToPaymentBtn: Button


    lateinit var recyclerView: RecyclerView
    var receivedList:ArrayList<CartModel> = ArrayList()

    lateinit var addressList:ArrayList<MutableMap<String, Any>>

    var adapter: OrderSummaryAdapter = OrderSummaryAdapter(receivedList)
    var addressMap:MutableMap<String,Any> = HashMap()

    var amountToPay1: Int = 0
    var amountToPay2: Int = 0

    var discount = 0
    var totalSellingPrice= 0
    var netSellingPrice = 0
    var deliveryCharge = 0L

    lateinit var  priceTxt:TextView
    lateinit var  discountTxt:TextView
    lateinit var  deliverChargeTxt:TextView
    lateinit var  amountTxt:TextView

    private val loadingDialog = LoadingDialog()
    private var thereIsAddressError = false
    private var thereIsPriceError = false
    var fromTo = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProceedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)



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

            if (thereIsAddressError or thereIsPriceError){

                continueToPaymentBtn.isEnabled = false
                continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.grey_600)
            }else{
                continueToPaymentBtn.isEnabled = true
                continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.purple_500)

                intent.putExtra("total_amount",amountToPay2)
                intent.putParcelableArrayListExtra("productList",receivedList)
                intent.putExtra("address",addressMap as Serializable)
                intent.putExtra("From_To",fromTo)
                //todo: 1=> MyCart / 2=> BuyNow
                startActivity(intent)
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

    private suspend fun calculateThePrice(list: ArrayList<CartModel> ){

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

    }

    private suspend fun setValuesInTextView(){
        thereIsPriceError = if (amountToPay1 ==amountToPay2){
            false
        }else{
            Toast.makeText(this,"Some problem in calculating the price", Toast.LENGTH_LONG).show()
            true
        }
        priceTxt.text = totalSellingPrice.toString()
        discountTxt.text = discount.toString()
        deliverChargeTxt.text = deliveryCharge.toString()
        amountTxt.text = amountToPay2.toString()
    }

}