package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MyAddressActivity
import com.sbdevs.bookonline.adapters.OrderSummaryAdapter
import com.sbdevs.bookonline.databinding.FragmentOrderSummaryBinding
import com.sbdevs.bookonline.models.CartModel

class OrderSummaryFragment : Fragment() {
    private var _binding: FragmentOrderSummaryBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser

    //var list:ArrayList<MutableMap<String,Any>> = ArrayList()
    lateinit var recyclerView:RecyclerView
    var recivdList:ArrayList<CartModel> = ArrayList()

    var adapter:OrderSummaryAdapter = OrderSummaryAdapter(recivdList)

    var totalAmount: Int = 0
    var discount = 0
    var totalPrice= 0

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

        totalPrice = intent.getIntExtra("total_price",-1)
        totalAmount = intent.getIntExtra("total_amount",-1)
        discount = intent.getIntExtra("total_discount",-1)

        adapter = OrderSummaryAdapter(recivdList)
        recyclerView.adapter = adapter


        priceTxt.text = totalPrice.toString()
        discountTxt.text = discount.toString()
        amountTxt.text = totalAmount.toString()





        binding.changeoraddAddressBtn.setOnClickListener {
            val intent = Intent(context, MyAddressActivity::class.java)
            intent.putExtra("from",2)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }

        binding.continueToPaymentBtn.setOnClickListener {
            val action = OrderSummaryFragmentDirections.actionOrderSummaryFragmentToPaymentFragment()
            findNavController().navigate(action)
        }


        return binding.root
    }

//    fun getFirebaseData3(){
//        firebaseFirestore.collection("USERS").document(user!!.uid)
//            .collection("USER_DATA").document("MY_CART")
//            .get().addOnCompleteListener {
//                if (it.isSuccessful){
//                    val x = it.result?.get("cart_list")
//                    if (x ==null){
//                        binding.emptyContainer.visibility = View.VISIBLE
//                        binding.linearLayout10.visibility =View.GONE
//                        binding.scrollviewCart.visibility = View.GONE
//                    }else{
//                        list = x as ArrayList<MutableMap<String, Any>>
//                        adapter.list = list
//                        adapter.notifyDataSetChanged()
//                        getProduct(list)
//                    }
//                }
//            }
//
//    }


}