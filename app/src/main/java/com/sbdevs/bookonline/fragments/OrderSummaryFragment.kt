package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderSummaryBinding.inflate(inflater, container, false)

        val loadingDialog : Dialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext, R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        loadingDialog.show()


        recyclerView = binding.summerRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false

        val priceTxt = binding.totalLay.totalPrice
        val discountTxt = binding.totalLay.totalDiscount
        val deliverCargeTxt = binding.totalLay.deliveryCharge
        val amountTxt = binding.totalLay.amountToPay

        val intent = requireActivity().intent
        val fromTo = intent.getIntExtra("From_To", -1)

        recivdList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>

        totalPrice1 = intent.getIntExtra("total_price",-1)
        totalAmount1 = intent.getIntExtra("total_amount",-1)
        discount1 = intent.getIntExtra("total_discount",-1)


        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
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
                    binding.continueToPaymentBtn.isEnabled = true
                }else{
                    Toast.makeText(context,"Some problem in calculating the price",Toast.LENGTH_SHORT).show()
                    binding.continueToPaymentBtn.isEnabled = false
                    priceTxt.text = totalPrice2.toString()
                    discountTxt.text = discount2.toString()
                    amountTxt.text = totalAmount2.toString()
                }
            }
        }


        adapter = OrderSummaryAdapter(recivdList)
        recyclerView.adapter = adapter

        binding.totalLay.totalItem.text = "( ${recivdList.size} item)"
        getAddress()

        binding.changeoraddAddressBtn.setOnClickListener {
            val intent = Intent(context, MyAddressActivity::class.java)
            intent.putExtra("from",2)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }



        binding.continueToPaymentBtn.setOnClickListener {
//            val action = OrderSummaryFragmentDirections.actionOrderSummaryFragmentToPaymentFragment()
//            findNavController().navigate(action)

            val intent = Intent(context, PaymentMethodActivity::class.java)
            intent.putExtra("total_amount",totalAmount2)
            intent.putParcelableArrayListExtra("productList",recivdList)
            intent.putExtra("address",sendingMap as Serializable)

            startActivity(intent)

        }


        return binding.root
    }

    fun getAddress(){

        val lay2 =  binding.miniAddress

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value, error ->
                error?.let {
                    Toast.makeText(context,"Problem in fetching address",Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                value?.let {
                    val position: Long = value.getLong("select_No")!!
                    val x = value.get("address_list")


                    if (x != null){
                        addressList = x as ArrayList<MutableMap<String, Any>>
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
                        binding.continueToPaymentBtn.isEnabled = true
                    }else{
                        binding.addressLay.visibility = View.GONE
                        binding.noAddress.visibility = View.VISIBLE
                        binding.continueToPaymentBtn.isEnabled = false
                    }


                }
            }

    }

}