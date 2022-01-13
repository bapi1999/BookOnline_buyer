package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MyAddressActivity
import com.sbdevs.bookonline.activities.PaymentMethodActivity
import com.sbdevs.bookonline.adapters.OrderSummaryAdapter
import com.sbdevs.bookonline.databinding.FragmentOrderSummaryBinding
import com.sbdevs.bookonline.models.CartModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import kotlin.text.StringBuilder

class OrderSummaryFragment : Fragment() {
    private var _binding: FragmentOrderSummaryBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private lateinit var continueToPaymentBtn:AppCompatButton


    lateinit var recyclerView:RecyclerView
    var recivdList:ArrayList<CartModel> = ArrayList()

    lateinit var addressList:ArrayList<MutableMap<String, Any>>

    var adapter:OrderSummaryAdapter = OrderSummaryAdapter(recivdList)

    var sendingMap:MutableMap<String,Any> = HashMap()

    var totalAmount1: Int = 0
    var totalAmount2: Int = 0
    var discount1 = 0
    var discount2 = 0
    var totalPrice1= 0
    var totalPrice2= 0


    private val loadingDialog = LoadingDialog()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSummaryBinding.inflate(inflater, container, false)

        recyclerView = binding.summerRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false

        val priceTxt = binding.totalLay.totalPrice
        val discountTxt = binding.totalLay.totalDiscount
        val deliverCargeTxt = binding.totalLay.deliveryCharge
        val amountTxt = binding.totalLay.amountToPay
        continueToPaymentBtn = binding.continueToPaymentBtn

        val intent = requireActivity().intent
        val fromTo = intent.getIntExtra("From_To", -1)

        recivdList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>

        totalPrice1 = intent.getIntExtra("total_price",-1)
        totalAmount1 = intent.getIntExtra("total_amount",-1)
        discount1 = intent.getIntExtra("total_discount",-1)


        lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.Main){

            }
            for ( group  in recivdList){

                val priceOriginal:Long = group.priceOriginal
                val priceSelling:Long = group.priceSelling
                val quantity:Long = group.orderQuantity

                if (priceOriginal == 0L){

                    totalAmount2 += priceSelling.toInt()*quantity.toInt()

                }else{
                    discount2 += (priceOriginal.toInt() - priceSelling.toInt())*quantity.toInt()
                    totalAmount2 += priceSelling.toInt()*quantity.toInt()

                }
                totalPrice2 = totalAmount2+discount2

            }
            delay(500)
            withContext(Dispatchers.Main){
                if (totalAmount1 ==totalAmount2){
                    priceTxt.text = totalPrice2.toString()
                    discountTxt.text = discount2.toString()
                    amountTxt.text = totalAmount2.toString()
                    continueToPaymentBtn.isEnabled = true
                }else{
                    Toast.makeText(context,"Some problem in calculating the price",Toast.LENGTH_SHORT).show()
                    continueToPaymentBtn.isEnabled = false
                    priceTxt.text = totalPrice2.toString()
                    discountTxt.text = discount2.toString()
                    amountTxt.text = totalAmount2.toString()
                }
            }

            withContext(Dispatchers.IO){
                getAddress()
            }


        }


        adapter = OrderSummaryAdapter(recivdList)
        recyclerView.adapter = adapter

        binding.totalLay.totalItem.text = "( ${recivdList.size} item)"


        binding.changeoraddAddressBtn.setOnClickListener {
            val intent = Intent(context, MyAddressActivity::class.java)
            intent.putExtra("from",2)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }






        return binding.root
    }

    override fun onStart() {
        super.onStart()

        continueToPaymentBtn.setOnClickListener {

            val intent = Intent(context, PaymentMethodActivity::class.java)
            intent.putExtra("total_amount",totalAmount2)
            intent.putParcelableArrayListExtra("productList",recivdList)
            intent.putExtra("address",sendingMap as Serializable)

            startActivity(intent)

        }
    }

    private fun getAddress(){

        val lay2 =  binding.miniAddress

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value, error ->
                error?.let {
                    Toast.makeText(context,"Problem in fetching address",Toast.LENGTH_SHORT).show()
                    Log.e("Get address","${it.message}")

                    binding.addressLay.visibility = View.GONE
                    binding.noAddress.visibility = View.VISIBLE
                    continueToPaymentBtn.isEnabled = false
                    continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_600)
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
                            sendingMap = addressList[position.toInt()]

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
                            binding.noAddress.visibility = View.GONE
                            continueToPaymentBtn.isEnabled = true
                            continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.primaryColor)
                            continueToPaymentBtn.text = "Continue"
                        }else{
                            binding.addressLay.visibility = View.GONE
                            binding.noAddress.visibility = View.VISIBLE
                            continueToPaymentBtn.isEnabled = false
                            continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_600)
                            continueToPaymentBtn.text = "No address found"
                        }

                    }else{

                        binding.addressLay.visibility = View.GONE
                        binding.noAddress.visibility = View.VISIBLE
                        continueToPaymentBtn.isEnabled = false
                        continueToPaymentBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_600)
                        continueToPaymentBtn.text = "No address found"
                    }


                }
            }

    }

}